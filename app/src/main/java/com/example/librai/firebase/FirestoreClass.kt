package com.example.librai.firebase

import android.util.Log
import com.example.librai.MainActivity
import com.example.librai.models.User
import com.example.librai.utils.Constants
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class FirestoreClass {

    private val mFirestore = FirebaseFirestore.getInstance()

    fun userSignUp(userInfo: User) {

        mFirestore.collection(Constants.USERS)
            //Document ID for users fields. Here the document is the User ID.
            .document(userInfo.id)
            .set(userInfo, SetOptions.merge())
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->
//                Log.e(
//                    activity.javaClass.simpleName,
//                    "Error while registering the user.",
//                    e
//                )
            }
    }
}