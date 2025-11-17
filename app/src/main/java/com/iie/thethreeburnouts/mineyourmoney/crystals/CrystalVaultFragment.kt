package com.iie.thethreeburnouts.mineyourmoney.crystals

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.iie.thethreeburnouts.mineyourmoney.MainActivity
import com.iie.thethreeburnouts.mineyourmoney.R
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentCrystalvaultBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrystalVaultFragment : Fragment() {

    private var _binding: FragmentCrystalvaultBinding? = null
    private val binding get() = _binding!!

    private lateinit var repo: CrystalRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrystalvaultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repo = CrystalRepository(requireContext())

        // Back button
        binding.topAppBar.setNavigationOnClickListener {
            (requireActivity() as MainActivity).replaceFragment(
                CrystalsFragment(), addToBackStack = false
            )
        }

        // Screenshot button
        binding.fabShare.setOnClickListener {
            takeScreenshotOfVault()
        }

        // Load crystals
        lifecycleScope.launch {
            loadCrystals()
        }
    }

    // -------------------------------
    // CAPTURE + SAVE + SHARE SCREENSHOT
    // -------------------------------
    private fun takeScreenshotOfVault() {

        val target = binding.scroll

        target.post {

            val bitmap = Bitmap.createBitmap(
                target.width,
                target.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            target.draw(canvas)

            val filename = "vault_${System.currentTimeMillis()}.png"
            val contentValues = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/MineYourMoney")
            }

            val resolver = requireContext().contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let { safeUri ->
                val outputStream = resolver.openOutputStream(safeUri)

                outputStream?.let { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()
                }

                // Share
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, safeUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(share, "Share Your Crystal Vault"))
            }
        }
    }



    // ----------------------------------
    // LOAD VAULT CONTENT
    // ----------------------------------
    private suspend fun loadCrystals() {
        val crystals = repo.getCrystals()

        val commons = crystals.filter { it.rarity == "COMMON" }
        val rares = crystals.filter { it.rarity == "RARE" }
        val legends = crystals.filter { it.rarity == "LEGENDARY" }

        fillList(binding.commonContainer, commons)
        fillList(binding.rareContainer, rares)
        fillList(binding.legendaryContainer, legends)
    }

    private fun fillList(container: ViewGroup, crystals: List<CrystalEntity>) {
        container.removeAllViews()

        if (crystals.isEmpty()) return

        val inflater = LayoutInflater.from(requireContext())
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        crystals.forEach { c ->
            val itemView = inflater.inflate(R.layout.item_vault_crystal, container, false)

            val icon = itemView.findViewById<ImageView>(R.id.crystal_icon)
            val rarityText = itemView.findViewById<TextView>(R.id.crystal_rarity)
            val dateText = itemView.findViewById<TextView>(R.id.crystal_date)

            rarityText.text = c.rarity
            dateText.text = dateFormat.format(Date(c.dateUnlocked))

            icon.setImageResource(R.drawable.ic_add_photo) // replace later if needed

            container.addView(itemView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
