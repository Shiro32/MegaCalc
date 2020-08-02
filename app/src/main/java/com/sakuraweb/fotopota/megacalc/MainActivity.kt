package com.sakuraweb.fotopota.megacalc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_main.*

var opPadString: String = ""

const val PAD_MODE_NUM1 = 1     // 左辺の数字（数字１）を入れている状態
const val PAD_MODE_OP   = 2     // 演算子を入れている状態
const val PAD_MODE_NUM2 = 3     // 右辺の数字（数字２）を入れている状態

var padMode = PAD_MODE_NUM1
var numPadString1: String = "0"
var numPadString2: String = ""

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
        numLCD.text = "0"
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
                FRAGMENT_NUM_PAD -> {
                    // 数字パッド画面。以下の２パターンがあり、演算子(opPadString）の有無で見分けている
                    // 　１．演算子を入力して戻ってくるケース
                    // 　２．演算子を入れてないけどスワイプで戻ってくるケース

                    when (opPadString ) {
                        // ルート（√）計算
                        // 単項演算子なので、数字２を入れる必要が無く、これで計算完了
                        // 計算完了時は OP_PAD画面にいる必要があるため遷移させる
                        getString(R.string.pad_root) -> {
                            padMode = PAD_MODE_NUM2
                            padPager.currentItem = FRAGMENT_OP_PAD
                        }

                        // OP_PADからスワイプで戻ってきたケース
                        "" -> {
                            if (padMode == PAD_MODE_NUM1) {
                                // まだ、数字１の入力途中なので何もしない
                            } else {
                                // 計算完了状態から強引に戻ってきたらAC扱い
                                numPadString1 = "0"
                                numLCD.text = numPadString1
                                padMode = PAD_MODE_NUM1
                            }
                        }
                    }
                }

                FRAGMENT_OP_PAD -> {
                    // 演算子パッド画面。以下の２パターンがある
                    // １．数字１の入力が終わり、自動遷移してくるケース


                    when (padMode) {
                        PAD_MODE_NUM1 -> {
                        }
                        PAD_MODE_NUM2 -> {
                            numPadString1 = calc( numPadString1, opPadString, numPadString2)
                            numLCD.text = numPadString1
                            numPadString2 = ""
                            opPadString = ""
                            opLCD.text = ""
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
        mHandler.postDelayed(changeToOpPad, 500)
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
                        startTimerToOpPad()
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
            getString(R.string.pad_plus), getString(R.string.pad_mul), getString(R.string.pad_div) -> {
                opPadString = char
                padMode = PAD_MODE_NUM2
                opLCD.text = opPadString
            }
            getString(R.string.pad_root) -> {
                opPadString = char
            }
            getString(R.string.pad_ac) -> {
                opPadString = ""
                padMode = PAD_MODE_NUM1
                numPadString1 = "0"
                numLCD.text = "0"
                numPadString2 = ""
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

        return "%.9s".format(ret.toString())
    }


}
