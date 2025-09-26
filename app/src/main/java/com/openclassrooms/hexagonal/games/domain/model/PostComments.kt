package com.openclassrooms.hexagonal.games.domain.model

import java.io.Serializable

data class PostComments(
    val id: String = "default",
    val postId: String = "default",
    val author: User? = null,
    val comment:String = "default",
) : Serializable


