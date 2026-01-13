package com.example.expensemanager.fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.expensemanager.R
import com.example.expensemanager.database.User
import com.example.expensemanager.databinding.FragmentProfileBinding
import com.example.expensemanager.viewmodel.UserViewModel
import java.util.Calendar


class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModelUser: UserViewModel by activityViewModels()
    private var userId: Int = -1
    private var currentUser: User? = null
    private var currProfile: Int = -1
    private var changeProfile: Int = -1
    private var selectedDate = ""
    private val avatarList = listOf(
        R.drawable.mu,
        R.drawable.avatar1,
        R.drawable.avatar2,
        R.drawable.avatar3,
        R.drawable.avatar4,
        R.drawable.avatar5,
        R.drawable.avatar6,
        R.drawable.avatar7,
        R.drawable.avatar8
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupListeners()

    }

    private fun setupObservers() {
        viewModelUser.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                userId = it.id
                currentUser = it
                displayUserData(it)
            }
        }

        viewModelUser.updateResult.observe(viewLifecycleOwner) { result ->
            binding.progressBar.visibility = View.GONE
            binding.btnSave.isEnabled = true

            result.onSuccess { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                }
            }

            result.onFailure { exception ->
                Toast.makeText(requireContext(), "Có lỗi xảy ra: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModelUser.imgProfile.observe(viewLifecycleOwner) { imgId ->
            imgId?.let {
                binding.imgProfile.setImageResource(imgId)
                currProfile = imgId
                changeProfile = currProfile
            }
        }
    }

    private fun setupListeners() {
        binding.imgProfile.setOnClickListener {
            showAvatarPicker()
        }

        binding.btnChangeImage.setOnClickListener {
            showAvatarPicker()
        }

        binding.etDob.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun displayUserData(user: User) {
        binding.etFullName.setText(user.fullName)
        binding.etDob.setText(user.dateOfBirth)
        binding.etAddress.setText(user.address)
        binding.etOccupation.setText(user.occupation)
        binding.tvUsername.text = "Tên đăng nhập: ${user.username}"

        selectedDate = user.dateOfBirth

    }

    private fun showAvatarPicker() {
        val avatarNames = avatarList.map { "Ảnh ${avatarList.indexOf(it) + 1}" }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Chọn ảnh đại diện")
            .setItems(avatarNames) { _, which ->
                binding.imgProfile.setImageResource(avatarList[which])
                changeProfile = avatarList[which]
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        if (selectedDate.isNotEmpty()) {
            val parts = selectedDate.split("-")
            if (parts.size == 3) {
                calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate =
                    String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.etDob.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun saveProfile() {
        val fullName = binding.etFullName.text.toString().trim()
        val dob = binding.etDob.text.toString().trim()
        val address = binding.etAddress.text.toString().trim()
        val occupation = binding.etOccupation.text.toString().trim()

        when{
            fullName.isEmpty() ->{
                Toast.makeText(requireContext(), "Vui lòng nhập họ tên", Toast.LENGTH_SHORT).show()
                return
            }
            dob.isEmpty() ->{
                Toast.makeText(requireContext(), "Vui lòng chọn ngày sinh", Toast.LENGTH_SHORT).show()
                return
            }
        }

        currentUser?.let { user ->

            val updatedUser = user.copy(
                fullName = fullName,
                dateOfBirth = dob,
                address = address,
                occupation = occupation
            )

            binding.progressBar.visibility = View.VISIBLE
            binding.btnSave.isEnabled = false
            viewModelUser.updateUser(updatedUser)

            if(currProfile != changeProfile){
                viewModelUser.loadImgProfile(changeProfile)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



}