package com.upsa.europapress.ui.home

import androidx.lifecycle.*
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import com.upsa.europapress.model.AllChannel
import com.upsa.europapress.utils.SingleLiveEvent
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel() {

    private val url = "https://rsshub-diygod.herokuapp.com/europapress"
    private val categories =
        mutableListOf(
            "",
            "/nacional",
            "/internacional",
            "/economia",
            "/deportes",
            "/cultura",
            "/sociedad",
            "/ciencia"
        )

    // 使用SingleLiveEvent 确保 Livedata 只发送一次
    private val _rssChannel = SingleLiveEvent<Channel>()
    val rssChannel: LiveData<Channel>
        get() = _rssChannel
    var status = SingleLiveEvent<Boolean?>()

    // 搜索
    private val _allChannel = SingleLiveEvent<AllChannel>()
    val allChannel: LiveData<AllChannel>
        get() = _allChannel

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

    fun searchFeed(parser: Parser) {
        viewModelScope.launch {
            try {
                var rssChannelS: Channel
                val allChannelS = AllChannel()
                for (i in categories) {
                    val channel = parser.getChannel(url + i)
                    rssChannelS = channel
                    allChannelS.allChannel.addAll(mutableListOf(rssChannelS))
                }
                _allChannel.postValue(allChannelS)
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
