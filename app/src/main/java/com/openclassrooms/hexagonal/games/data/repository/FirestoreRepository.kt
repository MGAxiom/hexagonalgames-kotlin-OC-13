package com.openclassrooms.hexagonal.games.data.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.PostComments
import jakarta.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    fun addPost(
        post: Post,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("posts")
            .document(post.id)
            .set(post)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun addCommentToPost(
        postId: String,
        comment: PostComments,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val postRef = firestore.collection("posts").document(postId)

        postRef.update("comments", FieldValue.arrayUnion(comment))
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

    fun getPost(
        postId: String,
        onSuccess: (Post?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("posts")
            .document(postId)
            .get()
            .addOnSuccessListener { snapshot ->
                val post = snapshot.toObject(Post::class.java)
                onSuccess(post)
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun getAllPosts(
        onSuccess: (List<Post>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("posts")
            .get()
            .addOnSuccessListener { snapshot ->
                val posts = snapshot.toObjects(Post::class.java)
                onSuccess(posts) }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}
