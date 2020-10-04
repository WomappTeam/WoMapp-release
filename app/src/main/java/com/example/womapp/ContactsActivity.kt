package com.example.womapp

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_contacts.*

class ContactsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)

        Button_Instagram.setOnClickListener {
            val url = "https://instagram.com/_womapp_?igshid=9ybf0ver0w7m"
            val instagram = Intent (Intent.ACTION_VIEW)
            instagram.data = Uri.parse(url)
            startActivity (instagram)
        }

        Button_Facebook.setOnClickListener {
            val url = "https://www.facebook.com/pg/Womapp-105767187816242/reviews/"
            val facebook = Intent(Intent.ACTION_VIEW)
            facebook.data = Uri.parse(url)
            startActivity(facebook)
        }

        Button_Twitter.setOnClickListener {
            val url = "https://twitter.com/Womapp2?s=08"
            val twitter = Intent(Intent.ACTION_VIEW)
            twitter.data = Uri.parse(url)
            startActivity(twitter)
        }

        Button_Sito.setOnClickListener {
            val url = "http://womapp.fauser.edu"
            val sito_web = Intent(Intent.ACTION_VIEW)
            sito_web.data = Uri.parse(url)
            startActivity(sito_web)
        }
    }
}
