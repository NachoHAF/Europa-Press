package com.upsa.europapress.model

import com.prof.rssparser.Channel
import java.io.Serializable

data class AllChannel(var allChannel: MutableList<Channel> = mutableListOf()) : Serializable