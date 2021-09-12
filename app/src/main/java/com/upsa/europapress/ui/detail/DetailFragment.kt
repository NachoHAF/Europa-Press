package com.upsa.europapress.ui.detail


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.kpstv.cwt.CWT
import com.squareup.picasso.Picasso
import com.upsa.europapress.R
import com.upsa.europapress.activities.MainActivity
import com.upsa.europapress.databinding.FragmentDetailBinding
import com.upsa.europapress.model.News
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import www.sanju.motiontoast.MotionToast
import java.text.SimpleDateFormat
import java.util.*


class DetailFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentDetailBinding? = null
    private val binding
        get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        databaseReference =
            FirebaseDatabase.getInstance(
                "https://europa-press-default-rtdb.europe-west1.firebasedatabase.app/"
            )
                .getReference("Users")

        databaseReference.keepSynced(true)

        tts = TextToSpeech(requireContext(), this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)

        // 监听实体返回按钮
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
           // if (binding.DetailFragment.visibility == View.VISIBLE) {}
           // binding.DetailFragment.visibility = View.INVISIBLE

           // if (tts != null) {
            //    tts!!.stop()
           //     tts!!.shutdown()
           // }
        }

        // 设置进度条
        binding.nestedScroll.setOnScrollChangeListener { _, _, scrollY, _, _
            ->
            val totalScrollLength =
                binding.nestedScroll.getChildAt(0).height - binding.nestedScroll.height
            Log.d("高度", totalScrollLength.toString())

            binding.progressBar.apply {
                max = totalScrollLength
                progress = scrollY

                if (progress == totalScrollLength || progress == 0) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(150)
                        binding.progressBar.visibility = View.INVISIBLE
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(150)
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }
            }

            // 设置收藏按钮吸顶
            val buttonToTopHeight = binding.buttonSave.top
            binding.progressBar.apply {
                max = totalScrollLength
                progress = scrollY

                if (progress <= buttonToTopHeight) {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(0)
                        binding.buttonSave2.visibility = View.INVISIBLE
                        binding.buttonSave.visibility = View.VISIBLE
                    }
                } else {
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(0)
                        binding.buttonSave2.visibility = View.VISIBLE
                        binding.buttonSave.visibility = View.INVISIBLE
                    }
                }
            }
        }

        return binding.root
    }

    private fun closeSoftKeyboard() {
        val iMm = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        iMm.hideSoftInputFromWindow(view?.windowToken, 0)
        view?.clearFocus()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)

        // 锁定侧边导航栏
        (activity as MainActivity).binding.drawerLayout.setDrawerLockMode(
            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        )

        closeSoftKeyboard()
        // 虚拟返回按钮
        binding.imageBack.setOnClickListener {

            // 解锁侧边导航栏
            (activity as MainActivity).binding.drawerLayout.setDrawerLockMode(
                DrawerLayout.LOCK_MODE_UNLOCKED
            )

            val transaction = (view.context as AppCompatActivity).supportFragmentManager
            transaction.popBackStack("fragmentContainer", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        // 监听实体返回按钮
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.DetailFragment.visibility == View.VISIBLE) {

                val transaction = (view.context as AppCompatActivity).supportFragmentManager
                transaction.popBackStack(
                    "fragmentContainer",
                    FragmentManager.POP_BACK_STACK_INCLUSIVE
                )
            }
        }

        // 获取Adapter传递的数值
        val touchTitle = arguments?.getString("touchTitle", "null")
        val touchTime = arguments?.getString("touchTime", "null")
        val touchChannel = arguments?.getString("touchChannel", "null")
        val touchImage =
            arguments?.getString(
                "touchImage",
                "https://mercadodelareina.es/files/sites/127/2018/02/756x485-europa-pres.jpg"
            )
        val touchImageDescription = arguments?.getString("touchImageDescription", "null")
        val touchContentDescription = arguments?.getString("touchContentDescription", "null")
        val touchLink = arguments?.getString("touchLink", "https://www.europapress.es/")

        val format = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH)
        val time = format.parse(touchTime ?: "Sun, 22 Aug 2021 16:17:08 GMT")
        // 2 AUGUST 2021 AT 12:31
        val pattern = "EEE, d MMMM yyyy aaa HH:mm"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.ENGLISH)
        val date = simpleDateFormat.format(time!!)

        // 插入layout
        binding.detailTitle.text = touchTitle
        binding.detailTime.text = date
        binding.channelTitle.text = touchChannel
        Picasso.get().load(touchImage).fit().centerCrop().into(binding.detailImage)
        binding.detailPhoto.text = touchImageDescription
        binding.detailContent.text = touchContentDescription

        // GO WEBSITE
        binding.buttonGoWeb.setOnClickListener {
            CWT.Builder(requireContext()).launch(touchLink.toString())
        }
        // 判定保存图标状态
        auth.currentUser?.uid?.let { s ->
            if (touchTime != null) {
                databaseReference
                    .child(s)
                    .child("favorites")
                    .child(touchTime)
                    .get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            binding.buttonSave.setCompoundDrawablesWithIntrinsicBounds(
                                context?.getDrawable(R.drawable.ic_favoritos_true),
                                null,
                                null,
                                null
                            )
                            binding.buttonSave2.setCompoundDrawablesWithIntrinsicBounds(
                                context?.getDrawable(R.drawable.ic_favoritos_true),
                                null,
                                null,
                                null
                            )
                        } else {
                            binding.buttonSave.setCompoundDrawablesWithIntrinsicBounds(
                                context?.getDrawable(R.drawable.ic_favoritos),
                                null,
                                null,
                                null
                            )
                            binding.buttonSave2.setCompoundDrawablesWithIntrinsicBounds(
                                context?.getDrawable(R.drawable.ic_favoritos),
                                null,
                                null,
                                null
                            )
                        }
                    }
            }
        }

        // 收藏文章按钮1
        binding.buttonSave.setOnClickListener {
            if (touchTime != null &&
                touchTitle != null &&
                touchChannel != null &&
                touchImage != null &&
                touchImageDescription != null &&
                touchContentDescription != null &&
                touchLink != null
            ) {
                favorite(
                    touchTime,
                    touchTitle,
                    touchChannel,
                    touchImage,
                    touchImageDescription,
                    touchContentDescription,
                    touchLink
                )
            }
        }

        // 收藏文章按钮2
        binding.buttonSave2.setOnClickListener {
            if (touchTime != null &&
                touchTitle != null &&
                touchChannel != null &&
                touchImage != null &&
                touchImageDescription != null &&
                touchContentDescription != null &&
                touchLink != null
            ) {
                favorite(
                    touchTime,
                    touchTitle,
                    touchChannel,
                    touchImage,
                    touchImageDescription,
                    touchContentDescription,
                    touchLink
                )
            }
        }

        // 设置阅读字体大小
        val fontSizeNormal = 18.5f
        val fontSizeBig = 22f
        val text = binding.detailContent
        var isNormal = true
        binding.imageFont.setOnClickListener {
            isNormal =
                if (isNormal) {
                    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeBig)
                    binding.imageFont.setImageResource(R.drawable.ic_fontsizebig)
                    false
                } else {
                    text.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeNormal)
                    binding.imageFont.setImageResource(R.drawable.ic_fontsizenormal)
                    true
                }
        }

        // 分享按钮
        binding.imageShare.setOnClickListener {
            val sendIntent: Intent =
                Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, touchLink)
                    type = "text/plain"
                }

            val shareIntent = Intent.createChooser(sendIntent, touchTitle)
            startActivity(shareIntent)
        }

        // 语音朗读
        val button = binding.buttonspeak
        val textAudio = binding.detailContent.text
        var click = false

        val position = 0
        val sizeOfChar: Int = textAudio.length
        val testStri: String = textAudio.substring(position, sizeOfChar)

        var next: Int
        var pos: Int

        @Suppress("DEPRECATION")
        button.setOnClickListener {
            next = 3999
            pos = 0
            if (!click) {
                Log.d("语音", "成功")
                tts =
                    TextToSpeech(context) {
                        if (it == TextToSpeech.SUCCESS) {
                            tts?.language = Locale("es", "ES")
                            tts?.setSpeechRate(1.0f)
                            while (true) {
                                MotionToast.createColorToast(
                                    requireActivity(),
                                    "Notice",
                                    "Start voice playback!",
                                    MotionToast.TOAST_SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(
                                        requireContext(),
                                        R.font.helvetica_regular
                                    )
                                )
                                var temp: String
                                try {
                                    temp = testStri.substring(pos, next)
                                    val params = HashMap<String, String>()
                                    params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = temp
                                    Log.d("语音3", "成功")
                                    tts?.speak(temp, TextToSpeech.QUEUE_ADD, params)
                                    pos += 3999
                                    next += 3999
                                    click = true
                                } catch (e: Exception) {
                                    Log.d("语音2", "成功")
                                    temp = testStri.substring(pos, testStri.length)
                                    tts?.speak(temp, TextToSpeech.QUEUE_ADD, null, null)
                                    click = true
                                    break
                                }
                            }
                        } else {
                            Log.e("TTS", "Initialization Failed!")
                            MotionToast.createColorToast(
                                requireActivity(),
                                "Warning",
                                "Initialization Failed!",
                                MotionToast.TOAST_ERROR,
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
                    "Stop voice playback!",
                    MotionToast.TOAST_SUCCESS,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                )
                if (tts != null) {
                    tts!!.stop()
                    tts!!.shutdown()
                }
                click = false
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun deleteData(touchTime: String) {
        val currentUser = auth.currentUser?.uid
        if (currentUser != null) {
            databaseReference
                .child(currentUser)
                .child("favorites")
                .child(touchTime)
                .removeValue()
                .addOnSuccessListener {
                    binding.buttonSave.setCompoundDrawablesWithIntrinsicBounds(
                        context?.getDrawable(R.drawable.ic_favoritos),
                        null,
                        null,
                        null
                    )
                    binding.buttonSave2.setCompoundDrawablesWithIntrinsicBounds(
                        context?.getDrawable(R.drawable.ic_favoritos),
                        null,
                        null,
                        null
                    )
                    MotionToast.createColorToast(
                        requireActivity(),
                        "Notice",
                        "Successfully Removed！",
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
                        "Unsuccessfully Removed！",
                        MotionToast.TOAST_ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                    )
                }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Suppress("DEPRECATION")
    private fun favorite(
        touchTime: String,
        touchTitle: String,
        touchChannel: String,
        touchImage: String,
        touchImageDescription: String,
        touchContentDescription: String,
        touchLink: String
    ) {
        val cm = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

        val currentUser = auth.currentUser?.uid
        if (currentUser != null) {
            databaseReference
                .child(currentUser)
                .child("favorites")
                .child(touchTime)
                .get()
                .addOnSuccessListener {
                    if (it.exists()) {
                        deleteData(touchTime)

                        if (!isConnected) {
                            binding.buttonSave.setCompoundDrawablesWithIntrinsicBounds(
                                context?.getDrawable(R.drawable.ic_favoritos),
                                null,
                                null,
                                null
                            )
                            binding.buttonSave2.setCompoundDrawablesWithIntrinsicBounds(
                                context?.getDrawable(R.drawable.ic_favoritos),
                                null,
                                null,
                                null
                            )
                            MotionToast.createColorToast(
                                requireActivity(),
                                "Notice",
                                "Successfully Removed！",
                                MotionToast.TOAST_SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                            )
                        }
                    } else {

                        if (!isConnected) {

                            binding.buttonSave.setCompoundDrawablesWithIntrinsicBounds(
                                context?.getDrawable(R.drawable.ic_favoritos_true),
                                null,
                                null,
                                null
                            )
                            binding.buttonSave2.setCompoundDrawablesWithIntrinsicBounds(
                                context?.getDrawable(R.drawable.ic_favoritos_true),
                                null,
                                null,
                                null
                            )
                            MotionToast.createColorToast(
                                requireActivity(),
                                "Notice",
                                "Successfully Saved！",
                                MotionToast.TOAST_SUCCESS,
                                MotionToast.GRAVITY_BOTTOM,
                                MotionToast.LONG_DURATION,
                                ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                            )
                        }

                        val news =
                            News(
                                touchTitle,
                                touchTime,
                                touchChannel,
                                touchImage,
                                touchImageDescription,
                                touchContentDescription,
                                touchLink
                            )
                        databaseReference
                            .child(currentUser)
                            .child("favorites")
                            .child(touchTime)
                            .setValue(news)
                            .addOnSuccessListener {
                                binding.buttonSave.setCompoundDrawablesWithIntrinsicBounds(
                                    context?.getDrawable(R.drawable.ic_favoritos_true),
                                    null,
                                    null,
                                    null
                                )
                                binding.buttonSave2.setCompoundDrawablesWithIntrinsicBounds(
                                    context?.getDrawable(R.drawable.ic_favoritos_true),
                                    null,
                                    null,
                                    null
                                )

                                MotionToast.createColorToast(
                                    requireActivity(),
                                    "Notice",
                                    "Successfully Saved！",
                                    MotionToast.TOAST_SUCCESS,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(
                                        requireContext(),
                                        R.font.helvetica_regular
                                    )
                                )
                            }
                            .addOnFailureListener {
                                binding.buttonSave.setCompoundDrawablesWithIntrinsicBounds(
                                    context?.getDrawable(R.drawable.ic_favoritos),
                                    null,
                                    null,
                                    null
                                )
                                MotionToast.createColorToast(
                                    requireActivity(),
                                    "Notice",
                                    "Unsuccessfully Saved！",
                                    MotionToast.TOAST_WARNING,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(
                                        requireContext(),
                                        R.font.helvetica_regular
                                    )
                                )
                            }
                    }
                }
                .addOnFailureListener {
                    MotionToast.createColorToast(
                        requireActivity(),
                        "Notice",
                        "Unsuccessfully Saved！",
                        MotionToast.TOAST_WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(requireContext(), R.font.helvetica_regular)
                    )
                }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("es", "ES"))
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Log.d("error", "Language not supported")
            } else {
                binding.buttonspeak.isEnabled = true
            }
        } else {
            Log.d("error", "Init failed")
        }
    }

    override fun onDestroyView() {
        // _binding = null
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }

        super.onDestroyView()
    }
}
