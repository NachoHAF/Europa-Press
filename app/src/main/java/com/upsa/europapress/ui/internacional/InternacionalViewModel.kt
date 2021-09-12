package com.upsa.europapress.ui.internacional

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import com.upsa.europapress.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import java.lang.Exception

class InternacionalViewModel : ViewModel() {
    private val url = "https://rsshub-diygod.herokuapp.com/europapress/internacional"

    // 使用SingleLiveEvent 确保 Livedata 只发送一次
    private val _rssChannel = SingleLiveEvent<Channel>()
    val rssChannel: LiveData<Channel>
        get() = _rssChannel

    var status = MutableLiveData<Boolean?>()

    fun fetchFeed(parser: Parser) {
        viewModelScope.launch {
            try {
                val channel = parser.getChannel(url)
                _rssChannel.postValue(channel)
            } catch (e: Exception) {
                e.printStackTrace()
                status.value = false
            }
        }
    }

    fun updateFeed(parser: Parser) {
        viewModelScope.launch {
            try {
                parser.flushCache(url)
                val channel = parser.getChannel(url)
                _rssChannel.postValue(channel)
            } catch (e: Exception) {
                e.printStackTrace()
                status.value = false
            }
        }
    }
}
