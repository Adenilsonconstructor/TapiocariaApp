package com.adenilsonsilva.tapiocariaapp

import android.app.Application
import android.util.Log
import com.adenilsonsilva.tapiocariaapp.database.TapiocariaDatabase
import com.adenilsonsilva.tapiocariaapp.repository.TapiocaRepository
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TapiocariaApplication : Application() {

    // Cria a instância do banco de dados de forma preguiçosa (só quando for usada pela primeira vez)
    val database: TapiocariaDatabase by lazy { TapiocariaDatabase.getDatabase(this) }

    // (NOVO) Cria a instância do repositório, usando os DAOs do nosso banco de dados.
    val repository: TapiocaRepository by lazy { TapiocaRepository(database.vendaDao(), database.saidaDao()) }

    override fun onCreate() {
        super.onCreate()
        loginAnonimoFirebase()
    }

    private fun loginAnonimoFirebase() {
        val auth = Firebase.auth
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (auth.currentUser == null) {
                    auth.signInAnonymously().await()
                    Log.d("FirebaseLogin", "Login anónimo realizado com sucesso.")
                } else {
                    Log.d("FirebaseLogin", "Utilizador já está logado.")
                }
            } catch (e: Exception) {
                Log.e("FirebaseLogin", "Erro ao fazer login anónimo", e)
            }
        }
    }
}