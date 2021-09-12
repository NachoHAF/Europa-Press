package com.upsa.europapress.ui.profile


import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.res.ResourcesCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.upsa.europapress.R
import com.upsa.europapress.activities.MainActivity
import com.upsa.europapress.activities.SignInActivity
import com.upsa.europapress.databinding.FragmentProfileBinding
import kotlinx.coroutines.*
import www.sanju.motiontoast.MotionToast
import java.util.regex.Pattern


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding
        get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private val fileResult = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        databaseReference =
            FirebaseDatabase.getInstance(
                "https://europa-press-default-rtdb.europe-west1.firebasedatabase.app/"
            )
                .getReference("Users")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 锁定侧边导航栏
        (activity as MainActivity).binding.drawerLayout.setDrawerLockMode(
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        )

        // 返回按钮
        binding.imageBack.setOnClickListener {
            val transaction = (view.context as AppCompatActivity).supportFragmentManager
            transaction.popBackStack("fragmentContainer", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        // 监听实体返回按钮
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val transaction = (view.context as AppCompatActivity).supportFragmentManager
            transaction.popBackStack("fragmentContainer", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        // 头像 用户名和邮箱显示
        binding.imageProfile.setOnClickListener { fileManager() }

        updateUI()

        // 退出按钮
        binding.buttonLoginOut.setOnClickListener { signOut() }

        // 修改用户名
        binding.buttonChangeName.setOnClickListener {
            val customDialog = Dialog(requireContext(), R.style.dialog_center)
            customDialog.setContentView(R.layout.dialog_custom_username)
            customDialog.setCanceledOnTouchOutside(true)

            val textField = customDialog.findViewById<EditText>(R.id.editTextTextPersonName).text
            val btnCancel = customDialog.findViewById<AppCompatButton>(R.id.btnNegative)
            val btnSend = customDialog.findViewById<AppCompatButton>(R.id.btnPositive)

            btnCancel.setOnClickListener { customDialog.dismiss() }

            btnSend.setOnClickListener {
                if (textField.isEmpty()) {
                    MotionToast.createColorToast(
                        requireActivity(),
                        "Notice",
                        "Please enter a valid username!",
                        MotionToast.TOAST_WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                    )
                } else {
                    updateUserName(textField.toString())
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(0)
                        customDialog.dismiss()
                    }
                }
            }

            customDialog.show()
        }

        // 修改密码
        binding.buttonChangePassword.setOnClickListener {
            val customDialog = Dialog(requireContext(), R.style.dialog_center)
            customDialog.setContentView(R.layout.dialog_custom_pwd)
            customDialog.setCanceledOnTouchOutside(true)

            val pwdActual =
                customDialog.findViewById<EditText>(R.id.editTextTextPasswordActual).text
            val pwdNew = customDialog.findViewById<EditText>(R.id.editTextTextPasswordChange).text
            val pwdRepeat =
                customDialog.findViewById<EditText>(R.id.editTextTextPasswordRepeat).text

            val btnCancel = customDialog.findViewById<AppCompatButton>(R.id.btnNegativePwd)
            val btnSend = customDialog.findViewById<AppCompatButton>(R.id.btnPositivePwd)

            btnCancel.setOnClickListener { customDialog.dismiss() }

            val passwordRegex =
                Pattern.compile(
                    "^" +
                            "(?=.*[-@#$%^/&+=])" + // Al menos 1 carácter especial
                            ".{6,}" + // Al menos 6 caracteres
                            "$"
                )

            btnSend.setOnClickListener {
                if (pwdActual.isEmpty()) {
                    MotionToast.createColorToast(
                        requireActivity(),
                        "Notice",
                        "Please enter the current password first!",
                        MotionToast.TOAST_WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                    )
                } else if (pwdNew.isEmpty() || !passwordRegex.matcher(pwdNew.toString()).matches()
                ) {
                    MotionToast.createColorToast(
                        requireActivity(),
                        "Notice",
                        "New Password should be more than 6 characters and contain at least one special character！",
                        MotionToast.TOAST_WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                    )
                } else if (pwdNew.toString() != pwdRepeat.toString()) {
                    MotionToast.createColorToast(
                        requireActivity(),
                        "Notice",
                        "Password twice inconsistent！",
                        MotionToast.TOAST_WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                    )
                } else {
                    chagePassword(pwdActual.toString(), pwdNew.toString())
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(0)
                        customDialog.dismiss()
                    }
                }
            }

            customDialog.show()
        }

        // 删除账号
        binding.buttonDelete.setOnClickListener {
            val customDialog = Dialog(requireContext(), R.style.dialog_center)
            customDialog.setContentView(R.layout.dialog_custom_delete)
            customDialog.setCanceledOnTouchOutside(true)

            val pwdCheck = customDialog.findViewById<EditText>(R.id.editTextTextPasswordCheck).text
            val btnCancel = customDialog.findViewById<AppCompatButton>(R.id.btnNegativeDelete)
            val btnSend = customDialog.findViewById<AppCompatButton>(R.id.btnPositiveDelete)

            btnCancel.setOnClickListener { customDialog.dismiss() }

            btnSend.setOnClickListener {
                if (pwdCheck.toString().isEmpty()) {
                    MotionToast.createColorToast(
                        requireActivity(),
                        "Notice",
                        "Password cannot be empty！",
                        MotionToast.TOAST_WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                    )
                } else {
                    deleteAccount(pwdCheck.toString())
                    customDialog.dismiss()
                }
            }

            customDialog.show()
        }
    }

    private fun signOut() {
        Firebase.auth.signOut()
        val intent = Intent(activity, SignInActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }

    private fun updateUI() {
        val currentUser = auth.currentUser?.uid

        if (currentUser != null) {
            databaseReference.child(currentUser).get().addOnSuccessListener {
                if (it.exists()) {
                    val username = it.child("username").value
                    binding.textUserName.text = username.toString()
                    binding.textUserEmail.text = auth.currentUser?.email
                }
            }
        }

        Picasso.get()
            .load(auth.currentUser?.photoUrl)
            .placeholder(R.drawable.userplaceholder)
            .centerCrop()
            .fit()
            .into(binding.imageProfile)
    }

    private fun updateUserName(userName: String) {
        val user = mapOf("username" to userName)
        auth.currentUser?.let {
            databaseReference
                .child(it.uid)
                .updateChildren(user)
                .addOnSuccessListener {
                    updateUI()
                    MotionToast.createColorToast(
                        requireActivity(),
                        "Notice",
                        "Successfully Updated！",
                        MotionToast.TOAST_SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                    )
                }
                .addOnFailureListener {
                    MotionToast.createColorToast(
                        requireActivity(),
                        "Notice",
                        "Failed to Update！",
                        MotionToast.TOAST_WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                    )
                }
        }
    }
    @Suppress("DEPRECATION")
    private fun fileManager() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, fileResult)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == fileResult) {
            if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
                val uri = data.data

                uri?.let { imageUpload(it) }
            }
        }
    }

    private fun imageUpload(mUri: Uri) {

        val user = auth.currentUser
        val folder: StorageReference = FirebaseStorage.getInstance().reference.child("Users")
        val fileName: StorageReference = folder.child("img" + user!!.uid)

        fileName
            .putFile(mUri)
            .addOnSuccessListener {
                fileName.downloadUrl.addOnSuccessListener { uri ->
                    val profileUpdates = userProfileChangeRequest {
                        photoUri = Uri.parse(uri.toString())
                    }
                    user.updateProfile(profileUpdates).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            updateUI()
                        }
                    }
                }
            }
            .addOnFailureListener { Log.i("TAG", "file upload error") }
    }

    private fun chagePassword(current: String, password: String) {
        val user = auth.currentUser

        if (user != null) {
            val email = user.email
            val credential = EmailAuthProvider.getCredential(email!!, current)

            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    user.updatePassword(password).addOnCompleteListener { taskUpdatePassword ->
                        if (taskUpdatePassword.isSuccessful) {
                            MotionToast.createColorToast(
                                requireActivity(),
                                "Notice",
                                "Password reset complete！",
                                MotionToast.TOAST_SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                            )
                        }
                    }
                } else {
                    MotionToast.createColorToast(
                        requireActivity(),
                        "Notice",
                        "The current password is incorrect！",
                        MotionToast.TOAST_WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                    )
                }
            }
        }
    }

    private fun deleteAccount(password: String) {
        val user = auth.currentUser

        if (user != null) {
            val email = user.email
            val credential = EmailAuthProvider.getCredential(email!!, password)

            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    user.delete().addOnCompleteListener { taskDeleteAcount ->
                        if (taskDeleteAcount.isSuccessful) {
                            MotionToast.createColorToast(
                                requireActivity(),
                                "Goodbye",
                                "The account has been successfully deleted!",
                                MotionToast.TOAST_ERROR,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                            )
                            signOut()
                        }
                    }
                } else {
                    MotionToast.createColorToast(
                        requireActivity(),
                        "Notice",
                        "the password entered is incorrect!",
                        MotionToast.TOAST_WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                    )
                }
            }
        }
    }
}
