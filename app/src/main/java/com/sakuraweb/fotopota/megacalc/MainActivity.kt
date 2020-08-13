package com.sakuraweb.fotopota.megacalc

import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.widget.Button
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_num_pad.*
import kotlin.math.*

// TODO: 桁あふれ時のEが無視されて、少数だけになってしまう・・・ （やっぱり、calc関数を大幅拡大せねば）
// TODO: 左スクロールで設定画面
// TODO: 起動時に、何桁表示できるかをチェックしよう（whileで探すしかないか？）
// TODO: フォントの二重重ねによる薄表示やりたい（完全モノスペースフォントが必要）

const val MIN_BUTTON_TEXT_SIZE = 10F

const val PAD_MODE_NUM1 = 1     // 左辺の数字（数字１）を入れている状態
const val PAD_MODE_OP   = 2     // 演算子を入れている状態
const val PAD_MODE_NUM2 = 3     // 右辺の数字（数字２）を入れている状態

var padMode = PAD_MODE_NUM1
var numPadString1: String = ""
var numPadString2: String = ""
var opPadString: String = ""

var maxDigits : Int = 9     // 表示可能最大桁数（これを自動で算出したいところだけど・・・）

class MainActivity : AppCompatActivity() {

    // ここがアプリのスタートポイント
    // といっても、全体枠をインフレートして、計算パッドのPagerをセットアップするくらい
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // padのPagerをセットする
        padPager.adapter = PadPagerAdapter(supportFragmentManager)
        padPager.addOnPageChangeListener(PageChangeListener())

        padMode = PAD_MODE_NUM1
        numPadString1="0"
        numLCD.text = "hello"
        pastLCD.text = "(c)2020 5hiro"

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        // 表示可能最大桁数を計算してみる
        val fontWidth = numLCD.paint.measureText("8")
        val lcdWidth = numLCD.measuredWidth

        maxDigits = lcdWidth / fontWidth.toInt()+1
    }

    // 計算パッドの画面変更リスナー
    // 文字パッド（FRAGMENT_NUM_PAD）と演算子パッド（FRAGMENT_OP_PAD）に切り替わった際の処理
    // 数字入力　→　1秒ほどで演算子入力に自動遷移
    // 演算入力　→　即時に数字入力
    // それぞれ、スワイプでも遷移可能なので割と複雑な処理になってしまった・・・。
    private inner class PageChangeListener() : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            // positionは遷移確定後のページ（NUM_PAD / OP_PAD）
            when (position) {

                // 数字パッド画面。以下の２パターンがあり、演算子(opPadString）の有無で見分けている
                // 　１．演算子を入力して戻ってくるケース
                // 　２．演算子を入れてないけどスワイプで戻ってくるケース
                FRAGMENT_NUM_PAD -> {

                    when (padMode) {

                        // 数字１の入力中に、OPに行ってNUMに戻ってきたパターン
                        // 何もせず、数字１の入力を続けていただく
                        PAD_MODE_NUM1 -> {
                        }

                        // 演算子入力中にスワイプで戻ってきた（＝数字の修正）
                        PAD_MODE_OP -> {
                            if (opPadString !== "") {
                                // 単項演算子（√など）は、opPadStringで見分けて、計算処理に戻す
                                // 数字２も入力済みとして、OP_PAD画面へ遷移
                                padMode = PAD_MODE_NUM2
                                padPager.currentItem = FRAGMENT_OP_PAD
                            } else {
                                // 演算子がまだ入っていないなら、数字１の入力修正に戻る
                                padMode = PAD_MODE_NUM1
                            }
                        }

                        // 計算完了状態で戻ってきたらＡＣ扱い
                        // それ以外は順当に数字２の入力をする
                        PAD_MODE_NUM2 -> {
                            if (opPadString == "") {
                                //　計算完了状態からスワイプして戻ってきたのでAC扱い
                                numPadString1 = "0"
                                numLCD.text = numPadString1
                                padMode = PAD_MODE_NUM1
                            }
                        }
                    }
                }

                FRAGMENT_OP_PAD -> {
                    // 演算子パッド画面。以下の２パターンがある
                    // １．数字１の入力が終わり、自動遷移してくるケース　MODE_NUM1
                    // ２．数字２の入力が終わり、自動遷移してくるケース MODE_NUM2
                    // ３．何も入れてないけど、スワイプしてくるケース
                    // ４．数字２を入れ終わってないのに、スワイプしてくるケース

                    when (padMode) {
                        // 数字１の入力が終わり、自動遷移してくるケース　MODE_NUM1
                        PAD_MODE_NUM1 -> {
                            // 演算子入力モードに遷移する
                            padMode = PAD_MODE_OP
                        }
                        // 演算子入力中に、NUM_PADに行って、さらにOP_PADに戻る
                        // 演算子の入れ直しということにしてあげる
                        PAD_MODE_OP -> {
                            opPadString = ""
                            opLCD.text = ""
                        }
                        // 数字２の入力中、入力完了でOP画面にくる
                        PAD_MODE_NUM2 -> {
                            if( numPadString2!="" || opPadString==getString(R.string.pad_root)) {
                                // 数字２入力完了 or 単項演算子の場合は計算して完了
                                val result = calc(numPadString1, opPadString, numPadString2)

                                pastLCD.text = "%s %s %s".format(numPadString1, opPadString, numPadString2)
                                numPadString1 = result
                                numLCD.text = numPadString1
                                numPadString2 = ""
                                opPadString = ""
                                opLCD.text = ""

                            } else {
                                // 入力未完了でスワイプしてくる（２項演算なのに、数字２が入ってない）
                                padMode = PAD_MODE_OP
                                opPadString = ""
                                opLCD.text = ""
                            }
                        }
                    }
                }
            }
        }
    }

    var mHandler = Handler()
    val changeToOpPad = object : Runnable {
        override fun run() {
            padPager.currentItem = 1
        }
    }
    private fun startTimerToOpPad() {
        mHandler.postDelayed(changeToOpPad, 800)
    }
    private fun stopTimerToOpPad() {
        mHandler.removeCallbacks(changeToOpPad)
    }

    // 数字パッドでボタンを押されたときの処理
    // 子Fragment（num_pad）で処理せず、全部こっちでやる。とても分かりやすい（＾＾）
    open fun clickNumPad( char: String ) {
        if( padMode== PAD_MODE_NUM1 ) {
            numPadString1 = makeNumber( char, numPadString1)
            numLCD.text = numPadString1
        }

        if( padMode== PAD_MODE_NUM2 ) {
            numPadString2 = makeNumber( char, numPadString2)
            numLCD.text = numPadString2
        }
    }

    private fun makeNumber( char: String, str: String ) : String {
        var padString = str

        // とりあえず自動画面切り替えタイマを止める
        stopTimerToOpPad()

        when (char) {
            "1","2","3","4","5","6","7","8","9" -> {
                if( padString == "0" ) {
                    padString = char
                } else {
                    padString += char
                }
                startTimerToOpPad()
            }
            "BS" -> {
                if( padString != "0" ) {
                    padString = padString.dropLast(1)
                    if( padString == "" ) {
                        padString = "0"
                    } else {
                        //startTimerToOpPad()
                    }
                }
            }
            "0" -> {
                // まだ何も入力されて無い時　→　１個だけ０は書ける（.で省略も可能だけど）
                // まだ「０」だけなので自動遷移タイマはセットしない
                if( padString == "" ) {
                    padString ="0"
                }
                else if( padString.indexOf(".") != -1) {
                    // すでに小数点が書かれていたら、どんどん追加できる
                    padString += "0"
                    // TODO: 本当は数字として意味が変な間は移行させたくないが（ex: 0.0010に0とか）
//                    startTimerToOpPad()
                } else if( padString.toDouble() != 0.0 ){
                    // 普通に桁を上げる０　（ex: 100に0）
                    padString += "0"
                    startTimerToOpPad()
                }
            }
            "." -> {
                // 何も入力されていないとき　→　"0."にしてあげる
                if (padString == "") {
                    padString = "0."
                    mHandler.removeCallbacks(changeToOpPad)
                }
                // まだ小数点が入っていないなら、追加する
                if (padString.indexOf(".") == -1) {
                    padString += "."
                    mHandler.removeCallbacks(changeToOpPad)
                }
            }
        }
        return padString
    }

    // 記号パッドでボタンが押されたときの処理
    // 子Fragment（op_pad）で処理せず、全部こっちで処理する。とても分かりやすい（＾＾）
    // TODO: いずれはnum_padから自動で飛んでくるようにする（Swipeではなく0.2秒くらいで）
    open fun clickOpPad( char: String ) {
        when( char ) {
            getString(R.string.pad_plus), getString(R.string.pad_mul), getString(R.string.pad_div), getString(R.string.pad_minus) -> {
                opPadString = char
                padMode = PAD_MODE_NUM2
                opLCD.text = opPadString
            }
            getString(R.string.pad_root) -> {
                opPadString = char
                padMode = PAD_MODE_OP
            }
            getString(R.string.pad_ac) -> {
                opPadString = ""
                opLCD.text = ""
                padMode = PAD_MODE_NUM1
                numPadString1 = "0"
                numPadString2 = ""
                numLCD.text = "0"
            }
        }

        // 左画面へ移行する
        padPager.currentItem = 0
    }


    // 結果を計算する！
    fun calc( lp: String, op: String, rp: String) : String {
        var ret: Double = 0.0

        when( op ) {
            getString(R.string.pad_plus)    -> { ret = lp.toDouble() + rp.toDouble() }
            getString(R.string.pad_minus)   -> { ret = lp.toDouble() - rp.toDouble() }
            getString(R.string.pad_mul)     -> { ret = lp.toDouble() * rp.toDouble() }
            getString(R.string.pad_div)     -> { ret = lp.toDouble() / rp.toDouble() }
            getString(R.string.pad_root)    -> { ret = kotlin.math.sqrt(lp.toDouble())
            }
        }


        // TODO:絶対値対応せねば！
        // 桁あふれ対応（NEW!） 9
        if( ret > 10.0.pow(maxDigits) ) {
            return "%1.${maxDigits-6}e".format(ret)
        }

        // 桁あふれ（小さい側）対応
        if( ret > 0 && ret < 10.0.pow(-(maxDigits-3)) ) {
            return "%1.${maxDigits-6}e".format(ret)
        }

        // 整数の場合。なぜか末尾に「.0」が付くのでサプレスしておく
        if( ret==ret.toInt().toDouble() ) {
            return "%.0f".format(ret)
        }

        // 普通に小数表示（Ｅ表示にしない）
//        return "%f".format(ret)
        return "%8s".format(ret.toString())
    }

    // ボタンテキストをいつでも最大化しよう！
    fun calcButtonTextSize( b: Button) : Float {
        val text = b.text.toString()

        val padding = b.paddingLeft
        val paint = Paint()

        val viewWidth = b.width - (padding*2)
        val viewHeight = b.height - (padding*2)

        var textSize = 200F

        paint.textSize = textSize
        var fm = paint.getFontMetrics()
        var textHeight = (Math.abs(fm.top))+(Math.abs(fm.descent))
        var textWidth = paint.measureText(text)

        while( viewWidth < textWidth || viewHeight < textHeight) {
            if( MIN_BUTTON_TEXT_SIZE >= textSize ) {
                textSize = MIN_BUTTON_TEXT_SIZE
                break
            }
            textSize -= 8F

            fm = paint.getFontMetrics()
            textHeight = (Math.abs(fm.top)) + (Math.abs(fm.descent))
            textWidth = paint.measureText(text)
        }

        b.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)

        return textSize


    }


}
