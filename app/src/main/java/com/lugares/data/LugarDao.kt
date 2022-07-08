package com.lugares.data

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase
import com.lugares.model.Lugar


class LugarDao {

    private val coleccion1="lugaresApp"
    private val usuarios=Firebase.auth.currentUser?.email.toString()
    /*private val usuarios="compartido" // para compartir entre usuarios los datos */
    private val coleccion2="misLugares"


    //obtener instancia de la base de datos
    private var firestore: FirebaseFirestore =  FirebaseFirestore.getInstance()

    init {
        firestore.firestoreSettings=FirebaseFirestoreSettings.Builder().build()
    }


    fun getAllData() : MutableLiveData<List<Lugar>> {
        val listaLugares = MutableLiveData<List<Lugar>>()

        // recuperar los documentos / lugares de nuestra coleccion mislugares
        firestore.collection(coleccion1).document(usuarios).collection(coleccion2)
            .addSnapshotListener{
                instantanea , e ->
                if (e != null){
                    return@addSnapshotListener
                }
                if (instantanea != null){
                    //si hay info y se recupera los datos
                    val lista = ArrayList<Lugar>()
                    //se recoge la instantanea
                    instantanea.documents.forEach{
                        val lugar = it.toObject(Lugar::class.java)
                        if (lugar != null){
                            lista.add(lugar)
                        }
                    }

                    listaLugares.value = lista
                }
            }
        return listaLugares
    }



    fun saveLugar(lugar : Lugar){
        val documento : DocumentReference
        if (lugar.id.isEmpty()){ //si no hay id es un lugar nuevo
            documento = firestore.collection(coleccion1).document(usuarios).collection(coleccion2).document()

            lugar.id = documento.id
        }else{
            documento = firestore.collection(coleccion1).document(usuarios).collection(coleccion2).document(lugar.id)
        }
        documento.set(lugar)
            .addOnSuccessListener {
                Log.e("saveLugar","Lugar agregado/modificado")

            }
            .addOnCanceledListener {
                Log.e("saveLugar","Lugar NO agregado/modificado")
            }
    }

    fun deleteLugar(lugar : Lugar){
        if (lugar.id.isNotEmpty()){
            //el lugar existe
            firestore.collection(coleccion1).document(usuarios).collection(coleccion2).document(lugar.id).delete()
                .addOnSuccessListener {
                    Log.e("deleLugar","Lugar eliminado")

                }
                .addOnCanceledListener {
                    Log.e("saveLugar","Lugar NO eliminado")
                }
        }

    }




}