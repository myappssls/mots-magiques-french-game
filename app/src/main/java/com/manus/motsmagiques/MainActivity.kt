package com.manus.motsmagiques

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.manus.motsmagiques.databinding.ActivityMainBinding
import com.manus.motsmagiques.databinding.DialogQuizBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState: Bundle?)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        observeViewModel()
        setupListeners()
    }

    private fun observeViewModel() {
        viewModel.xp.observe(this) { xp ->
            binding.tvXp.text = getString(R.string.xp_label, xp)
        }

        viewModel.level.observe(this) { level ->
            binding.tvLevel.text = getString(R.string.level_label, level)
        }

        viewModel.streak.observe(this) { streak ->
            binding.tvStreak.text = getString(R.string.streak_label, streak)
        }
    }

    private fun setupListeners() {
        binding.btnTimeAttack.setOnClickListener {
            startQuiz()
        }

        binding.btnAddFromInternet.setOnClickListener {
            val word = binding.etNewWord.text.toString().trim()
            if (word.isNotEmpty()) {
                viewModel.addNewWordFromInternet(word)
                binding.etNewWord.text.clear()
                Toast.makeText(this, "جاري البحث عن ترجمة...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "يرجى إدخال كلمة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startQuiz() {
        val words = viewModel.allWords.value
        if (words.isNullOrEmpty()) {
            Toast.makeText(this, "جاري تحميل الكلمات...", Toast.LENGTH_SHORT).show()
            return
        }

        val randomWord = words.random()
        val options = mutableListOf(randomWord.arabicTranslation)
        val otherWords = words.filter { it.id != randomWord.id }.shuffled()
        
        if (otherWords.size >= 2) {
            options.add(otherWords[0].arabicTranslation)
            options.add(otherWords[1].arabicTranslation)
        } else {
            options.add("خطأ 1")
            options.add("خطأ 2")
        }
        options.shuffle()

        showQuizDialog(randomWord, options)
    }

    private fun showQuizDialog(word: WordEntity, options: List<String>) {
        val dialogBinding = DialogQuizBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(true)
            .create()

        dialogBinding.tvFrenchWord.text = word.frenchWord
        dialogBinding.rbOption1.text = options[0]
        dialogBinding.rbOption2.text = options[1]
        dialogBinding.rbOption3.text = options[2]

        viewModel.aiExplanation.observe(this) { explanation ->
            if (explanation != null) {
                dialogBinding.tvAiExplanation.text = explanation
                dialogBinding.tvAiExplanation.visibility = View.VISIBLE
            }
        }

        dialogBinding.btnAiExplain.setOnClickListener {
            dialogBinding.btnAiExplain.isEnabled = false
            dialogBinding.tvAiExplanation.text = "جاري التحليل..."
            dialogBinding.tvAiExplanation.visibility = View.VISIBLE
            viewModel.fetchAiExplanation(word.frenchWord)
        }

        dialogBinding.btnSubmit.setOnClickListener {
            val selectedId = dialogBinding.rgOptions.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(this, "يرجى اختيار إجابة", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadioButton = dialogBinding.root.findViewById<RadioButton>(selectedId)
            val answer = selectedRadioButton.text.toString()

            if (answer == word.arabicTranslation) {
                Toast.makeText(this, "إجابة صحيحة! 🎉", Toast.LENGTH_SHORT).show()
                viewModel.onCorrectAnswer(word)
                dialog.dismiss()
                startQuiz()
            } else {
                Toast.makeText(this, "حاول مرة أخرى ❌", Toast.LENGTH_SHORT).show()
                viewModel.onIncorrectAnswer(word)
            }
        }

        dialog.show()
    }
}
