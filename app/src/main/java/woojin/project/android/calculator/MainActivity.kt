package woojin.project.android.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.room.Room
import woojin.project.android.calculator.model.History
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity() {

    private var isOperator: Boolean = false //연산자가 이미 입력되었는지 확인하는 flag
    private var hasOperator: Boolean = false //연산자가 한개만 입력되었는지 확인하는 flag

    private val expressionTexView: TextView by lazy {
        findViewById<TextView>(R.id.expressionTextView)
    }

    private val resultTextView: TextView by lazy {
        findViewById<TextView>(R.id.resultTextView)
    }

    private val historyLayout:View by lazy {
        findViewById<View>(R.id.historyLayout)
    }

    private val historyLinearLayout:LinearLayout by lazy {
        findViewById<LinearLayout>(R.id.historyLinearLayout)
    }

    lateinit var db:AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "historyDB").build()
    }

    fun buttonClicked(v: View) {
        when (v.id) {
            R.id.button0 -> numberButtonClicked("0")
            R.id.button1 -> numberButtonClicked("1")
            R.id.button2 -> numberButtonClicked("2")
            R.id.button3 -> numberButtonClicked("3")
            R.id.button4 -> numberButtonClicked("4")
            R.id.button5 -> numberButtonClicked("5")
            R.id.button6 -> numberButtonClicked("6")
            R.id.button7 -> numberButtonClicked("7")
            R.id.button8 -> numberButtonClicked("8")
            R.id.button9 -> numberButtonClicked("9")
            R.id.buttonPlus -> operatorButtonClicked("+")
            R.id.buttonMinus -> operatorButtonClicked("-")
            R.id.buttonMulti -> operatorButtonClicked("*")
            R.id.buttonDivider -> operatorButtonClicked("/")
            R.id.buttonModul -> operatorButtonClicked("%")
        }
    }

    private fun numberButtonClicked(number: String) {

        //연산자를 누르다 왔으면
        if (isOperator) {
            //숫자 입력이 시작됨을 알 수 있도록 공백 1칸 추가적으로 표시해 줌
            expressionTexView.append(" ")
        }

        isOperator = false

        val expressionText = expressionTexView.text.split(" ")

        if (expressionText.isNotEmpty() && expressionText.last().length >= 15) {
            Toast.makeText(this, "15자리까지만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        } else if (number == "0" && expressionText.last().isEmpty()) {
            Toast.makeText(this, "0은 제일 앞에 올 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        expressionTexView.append(number)

        //resultTextView에 실시간으로 계산 결과 반영
        resultTextView.text = calculateExpression()
    }

    private fun operatorButtonClicked(operator: String) {
        //최초에 연산자가 제일 먼저 입력됐으면 무시함
        if (expressionTexView.text.isEmpty()) {
            return
        }

        when {
            //연산자가 이미 입력되어 있는데 연산자를 한번 더 입력하면 후자것으로 교체
            isOperator -> {
                val text = expressionTexView.text.toString()
                expressionTexView.text = text.dropLast(1) + operator
            }

            //이미 두 수에 대한 연산자 입력이 되어 있을 때는 연산자 추가를 방지
            hasOperator -> {
                Toast.makeText(this, "연산자는 한번만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                return
            }

            else -> {
                expressionTexView.append(" $operator")
            }
        }

        //연산자만 초록색 표시
        val ssb = SpannableStringBuilder(expressionTexView.text)
        ssb.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.green)), expressionTexView.text.length - 1, expressionTexView.text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        expressionTexView.text = ssb

        isOperator = true
        hasOperator = true
    }

    fun clearButtonClicked(v: View) {
        expressionTexView.text = ""
        resultTextView.text = ""
        isOperator = false
        hasOperator = false
    }

    fun resultButtonClicked(v: View) {
        val expressionTexts = expressionTexView.text.split(" ")

        if (expressionTexView.text.isEmpty() || expressionTexts.size == 1) {
            return
        }

        if (expressionTexts.size != 3 && hasOperator) {
            Toast.makeText(this, "아직 완성되지 않은 수식입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        if (expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()) {
            Toast.makeText(this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val expressionText = expressionTexView.text.toString()
        val resultText = calculateExpression()

        //DB에 결과 넣어줌
        //DB IO는 별도 스레드에서 작업되어야 함
        Thread(Runnable {
            db.historyDao().insertHistory(History(null, expressionText, resultText))
        }).start()

        resultTextView.text = ""
        expressionTexView.text = resultText

        isOperator = false
        hasOperator = false
    }

    private fun calculateExpression(): String {
        val expressionTexts = expressionTexView.text.split(" ")

        if (hasOperator.not() || expressionTexts.size != 3) {
            return ""
        } else if (expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()) {
            return ""
        }

        val exp1 = expressionTexts[0].toBigInteger()
        val exp2 = expressionTexts[2].toBigInteger()
        val op = expressionTexts[1]

        return when (op) {
            "+" -> (exp1 + exp2).toString()
            "-" -> (exp1 - exp2).toString()
            "*" -> (exp1 * exp2).toString()
            "/" -> (exp1 / exp2).toString()
            "%" -> (exp1 % exp2).toString()
            else -> ""
        }
    }

    fun historyButtonClicked(v: View) {
        //뷰에 보이기
        historyLayout.isVisible = true
        historyLinearLayout.removeAllViews()

        //DB에서 히스토리 내용 가져오기
        Thread(Runnable {
            db.historyDao().getAll().reversed().forEach {
                runOnUiThread {
                    val historyView = LayoutInflater.from(this).inflate(R.layout.history_row, null, false)

                    historyView.findViewById<TextView>(R.id.expressionTextView).text = it.expression
                    historyView.findViewById<TextView>(R.id.resultTextView).text = "= ${it.result}"

                    historyLinearLayout.addView(historyView)
                }
            }

        }).start()
    }

    fun clearHistoryButtonClicked(v:View){
        //뷰 지우기
        historyLinearLayout.removeAllViews()

        //DB에서 지우기
        Thread(Runnable {
            db.historyDao().deleteAll()
        }).start()
    }

    fun closeHistoryButtonClicked(v:View){
        historyLayout.isVisible = false
    }

}

fun String.isNumber(): Boolean {
    return try {
        this.toBigInteger()
        true
    } catch (e: NumberFormatException) {
        false
    }
}