package com.lugares.ui.lugar

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import android.widget.Toast.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.lugares.R
import com.lugares.databinding.FragmentUpdateLugarBinding
import com.lugares.model.Lugar
import com.lugares.viewmodel.LugarViewModel
import java.util.jar.Manifest

class UpdateLugarFragment : Fragment() {

    private val args by navArgs<UpdateLugarFragmentArgs>()

    private lateinit var lugarViewModel: LugarViewModel

    private var _binding: FragmentUpdateLugarBinding? = null
    private val binding get() = _binding!!

    private lateinit var mediaPlayer: MediaPlayer


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lugarViewModel = ViewModelProvider(this)[LugarViewModel::class.java]
        _binding = FragmentUpdateLugarBinding.inflate(inflater,container,false)

        binding.etNombre.setText(args.lugar.nombre)
        binding.etTelefono.setText(args.lugar.telefono)
        binding.etCorreo.setText(args.lugar.correo)
        binding.etWeb.setText(args.lugar.web)

        binding.btActualizar.setOnClickListener { updateLugar()}

        binding.tvAltura.text=args.lugar.altura.toString()
        binding.tvLatitud.text=args.lugar.altura.toString()
        binding.tvLongitud.text=args.lugar.altura.toString()

        binding.btEmail.setOnClickListener{ escribirCorreo()}
        binding.btPhone.setOnClickListener{ llamarLugar()}
        //binding.btWhatsapp.setOnClickListener{ enviarWhatsApp()}
        binding.btWeb.setOnClickListener{ verWeb()}
        //binding.btLocation.setOnClickListener{ verMapa()}
        binding.btPlay.setOnClickListener { mediaPlayer.start() }

        binding.btWhatsapp.setOnClickListener { enviarWhatsApp() }
        //indica que en la pantalla se agrega opcion de menu

        //para iniciar el boton de play
        if (args.lugar.rutaAudio?.isNotEmpty() == true){
            mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(args.lugar.rutaAudio)
            mediaPlayer.prepare()
            binding.btPlay.isEnabled=true
            binding.btPlay.setOnClickListener { mediaPlayer.start() }
        } else {
            binding.btPlay.isEnabled=false
        }
        // ruta hay ruta imagen la dibujo
        if (args.lugar.rutaImagen?.isNotEmpty() == true){
            Glide.with(requireContext())
                .load(args.lugar.rutaImagen)
                .fitCenter()
                .into(binding.imagen)
        }

        binding.btLocation.setOnClickListener { verMapa() }
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun verMapa(){
        val latitud=binding.tvLatitud.text.toString().toDouble()
        val longitud=binding.tvLongitud.text.toString().toDouble()
        if (latitud.isFinite() && longitud.isFinite()){
            val location = Uri.parse("geo:$latitud,$longitud?z18")
            val mapIntent = Intent(Intent.ACTION_VIEW,location)
            startActivity(mapIntent)
        }else{

        }
    }
    private fun enviarWhatsApp() {

        val telefono = binding.etTelefono.text.toString()
        if (telefono.isNotEmpty()){

            val sendIntent = Intent(Intent.ACTION_VIEW)
            val uri = "whatsapp://send?phone=506$telefono&text="+getString(R.string.msg_saludos)
            sendIntent.setPackage("com.whatsapp")
            sendIntent.data=Uri.parse(uri)
            startActivity(sendIntent)

        }else{
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_datos),Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun llamarLugar() {

        val recurso = binding.etTelefono.text.toString()
        if(recurso.isNotEmpty()) {

            val rutina = Intent(Intent.ACTION_CALL)
            rutina.data = Uri.parse("tel:$recurso")
            if(requireActivity().checkSelfPermission(android.Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED){

                requireActivity().requestPermissions(arrayOf(android.Manifest.permission.CALL_PHONE),
                    105)
            }else{
                requireActivity().startActivity(rutina)
            }
        } else {
            Toast.makeText(requireContext(),getString(R.string.msg_datos),Toast.LENGTH_SHORT)
        }
    }

    private fun escribirCorreo() {

        val recurso = binding.etCorreo.text.toString()
        if (recurso.isNotEmpty()){
            //se activa el correo
            val rutina = Intent(Intent.ACTION_SEND)
            rutina.type="message/rfc822"
            rutina.putExtra(Intent.EXTRA_EMAIL, arrayOf(recurso))
            rutina.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.msg_saludos)+" "+binding.etNombre.text)
            rutina.putExtra(Intent.EXTRA_TEXT,getString(R.string.msg_mensaje_correo))
            startActivity(rutina) // Levanta el correo y lo presenta

        }else{
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_datos),Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun verWeb() {

        val recurso = binding.etWeb.text.toString()
        if (recurso.isNotEmpty()){
            //se activa el correo
            val rutina = Intent(Intent.ACTION_VIEW, Uri.parse("http://$recurso"))
            startActivity(rutina)// Levanta el correo y lo presenta

        }else{
            Toast.makeText(
                requireContext(),
                getString(R.string.msg_datos),Toast.LENGTH_SHORT
            ).show()
        }
    }

    //se genera el icono de borrar en la vista
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu,menu)
    }

    // se detecta un click en el icono borrar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //pregunto si dio click en el icono de borrado
        if (item.itemId==R.id.menu_delete)
            //hace la accion al darle click
            deleteLugar()
        return super.onOptionsItemSelected(item)
    }

    private fun deleteLugar() {
        val consulta = AlertDialog.Builder(requireContext())
        consulta.setTitle(R.string.delete) //Titulo a la ventana
        consulta.setMessage(getString(R.string.seguroBorrar)+ " ${args.lugar.nombre}")

        //acciones a ejecutar si responde si
        consulta.setPositiveButton(getString(R.string.si)){ _,_ ->

            //borramos el lugar (sin consultar)
            lugarViewModel.deleteLugar(args.lugar)
            findNavController().navigate(R.id.action_updateLugarFragment_to_nav_lugar)
        }
        consulta.setNegativeButton(getString(R.string.no)){
                _,_ ->
        }

        consulta.create().show()
    }

    private fun updateLugar() {
        val nombre=binding.etNombre.text.toString()
        val correo=binding.etCorreo.text.toString()
        val telefono=binding.etTelefono.text.toString()
        val web=binding.etWeb.text.toString()

        if(nombre.isNotEmpty()) {
            val lugar = Lugar(args.lugar.id,nombre,correo,telefono,web,0.0,0.0,0.0,"","")
            lugarViewModel.saveLugar(lugar)
            makeText(requireContext(),getString(R.string.lugarAdded), LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_updateLugarFragment_to_nav_lugar)
        } else {
            makeText(requireContext(),getString(R.string.noData), LENGTH_SHORT).show()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}