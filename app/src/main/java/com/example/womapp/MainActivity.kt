package com.example.womapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.net.Uri

class MainActivity : AppCompatActivity() {  //classe Schermo principale

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
/*
        EVENTI GESTIONE BOTTONI
 */
        //inizialize Toolbar
        setSupportActionBar(toolBarMain);

        //Evento click bottone --> attività di log-in
        startButton.setOnClickListener {
           startActivity(Intent(this, LogInActivity::class.java))
        }

        //Evento click bottone --> collegamento url a pagina instagram
        instagramButton.setOnClickListener {
            val url = "https://instagram.com/_womapp_?igshid=9ybf0ver0w7m"
            val instagram = Intent(Intent.ACTION_VIEW)
            instagram.data = Uri.parse(url)
            startActivity(instagram)
        }

        //Evento click bottone --> collegamento url a pagina facebook
        facebookbutton.setOnClickListener {
            val url = "https://www.facebook.com/pg/Womapp-105767187816242/reviews/"
            val facebook = Intent(Intent.ACTION_VIEW)
            facebook.data = Uri.parse(url)
            startActivity(facebook)
        }

        //Evento click bottone --> collegamento url a pagina twitter
        twitterButton.setOnClickListener {
            val url = "https://twitter.com/Womapp2?s=09"
            val twitter = Intent(Intent.ACTION_VIEW)
            twitter.data = Uri.parse(url)
            startActivity(twitter)
        }

    }
}
