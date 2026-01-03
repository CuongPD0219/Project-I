package com.example.expensemanager.activities

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.expensemanager.database.AppDatabase
import com.example.expensemanager.database.User
import com.example.expensemanager.databinding.ActivityProfileBinding
import com.example.expensemanager.viewmodel.UserViewModel
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Calendar



class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val viewModel: UserViewModel by viewModels()
    private var userId: Int = -1
    private var currentUser: User? = null
    private var selectedImageBase64: String? = null
    private var selectedDate = ""


    // xử lý ảnh trả về từ một ứng dụng khác thông qua Intent
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> // result chứa tất cả thông tin ảnh trả về
        if (result.resultCode == RESULT_OK) { // nếu thành công
            result.data?.data?.let { uri ->
                handleImageSelection(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tuỳ chỉnh thanh công cụ phía trên của màn hình
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Thông tin cá nhân"

        userId = intent.getIntExtra("userId",-1)

        if(userId == -1){
            Toast.makeText(this@ProfileActivity, "Không thể tải dữ liệu người dùng", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupObserves()
        setupListeners()
        viewModel.loadUser(userId)
    }


    private fun setupObserves(){
        viewModel.user.observe(this){ user ->
            user?.let{
                currentUser = it
                displayUserData(it)
            }
        }

        viewModel.updateResult.observe(this){ result ->
            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true

            result.onSuccess{ success ->
                if(success) {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(this, "Co loi xay ra", Toast.LENGTH_SHORT).show()
                }
            }

            result.onFailure{exception->
                Toast.makeText(this, "Co loi xay ra: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayUserData(user: User){
        binding.etFullName.setText(user.fullName)
        binding.etDob.setText(user.dateOfBirth)
        binding.etAddress.setText(user.address)
        binding.etOccupation.setText(user.occupation)
        binding.tvUsername.text = "Ten dang nhap: ${user.username}"

        selectedDate = user.dateOfBirth

        user.profileImage?.let{ base64Image ->
            if(base64Image.isNotEmpty()){
                try{
                    val imageBytes = Base64.decode(base64Image, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.imgProfile.setImageBitmap(bitmap)
                }catch(e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    // mở ảnh
    private fun openImagePicker(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun setupListeners(){
        binding.imgProfile.setOnClickListener {
            openImagePicker()
        }

        binding.btnChangeImage.setOnClickListener {
            openImagePicker()
        }

        binding.etDob.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun handleImageSelection(uri: Uri){
        try{
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG,80,outputStream)
            val imageBytes = outputStream.toByteArray()
            selectedImageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)

            binding.imgProfile.setImageBitmap(bitmap)
        }catch(e: Exception){
            Toast.makeText(this, "Khong the tai anh", Toast.LENGTH_SHORT).show()
        }
    }

    // hiển thị lịch để người dùng chọn ngày tháng hợp lệ
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        if (selectedDate.isNotEmpty()) {
            val parts = selectedDate.split("-")
            if (parts.size == 3) {// lịch hợp lệ
                calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt()) // tháng trong Calendar từ 0 -> 11
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate =
                    String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.etDob.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    // lưu thông tin người dùng lên cơ sở dữ liệu và ứng dụng
    private fun saveProfile(){
        val fullName = binding.etFullName.text.toString().trim()
        val dateOfBirth = binding.etDob.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val occupation = binding.etOccupation.text.toString().trim()

        if(fullName.isEmpty()){
            Toast.makeText(this, "Vui long nhap ho ten", Toast.LENGTH_SHORT).show()
            return
        }

        currentUser?.let{user->
            val updateUser = user.copy(
                fullName = fullName,
                dateOfBirth = dateOfBirth,
                address = address,
                occupation = occupation,
                profileImage = selectedImageBase64?: user.profileImage
            )

            binding.progressBar.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false
            viewModel.updateUser(updateUser)
        }
    }

    // xử lý khi người dùng nhấn nút Back
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true

    }
}