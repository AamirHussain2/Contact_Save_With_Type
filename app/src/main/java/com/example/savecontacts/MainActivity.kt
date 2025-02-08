package com.example.savecontacts

import android.Manifest
import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.material.textfield.TextInputLayout
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {

    private val permissions = arrayOf(
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_PHONE_STATE
    )

    private var contactName: String = ""
    private var contactNumber: String = ""
    private var contactEmail: String = "abc@gmail.com"
    private var contactBirthday: String = "12-09-2003"
    private var contactAddress: String = "Islamabad"
    private var contactSaveTo: String = ""


    companion object {
        private lateinit var SAVE_TO: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nameInput = findViewById<TextInputLayout>(R.id.editTextText)
        val numberInput = findViewById<TextInputLayout>(R.id.editTextPhone)
        val sim1Button = findViewById<Button>(R.id.button)
        val sim2Button = findViewById<Button>(R.id.button2)
        val googleButton = findViewById<Button>(R.id.button3)
        val whatsAppButton = findViewById<Button>(R.id.button4)
        val phoneButton = findViewById<Button>(R.id.button5)

        sim1Button.setOnClickListener {
            contactName = nameInput.editText?.text.toString()
            contactNumber = numberInput.editText?.text.toString()
            contactSaveTo = "Sim1"
            SAVE_TO = "Sim"
            handlePermissionsAndSaveContact()
        }

        sim2Button.setOnClickListener {
            contactName = nameInput.editText?.text.toString()
            contactNumber = numberInput.editText?.text.toString()
            contactSaveTo = "Sim2"
            SAVE_TO = "Sim"
            handlePermissionsAndSaveContact()
        }

        googleButton.setOnClickListener {
            contactName = nameInput.editText?.text.toString()
            contactNumber = numberInput.editText?.text.toString()
            contactSaveTo = "com.google"
            SAVE_TO = "Account"
            handlePermissionsAndSaveContact()
        }

        whatsAppButton.setOnClickListener {
            contactName = nameInput.editText?.text.toString()
            contactNumber = numberInput.editText?.text.toString()
            contactSaveTo = "com.whatsapp"
            SAVE_TO = "Account"
            handlePermissionsAndSaveContact()
        }

        phoneButton.setOnClickListener {
            contactName = nameInput.editText?.text.toString()
            contactNumber = numberInput.editText?.text.toString()
            SAVE_TO = ""
            handlePermissionsAndSaveContact()
        }
    }

    private fun handlePermissionsAndSaveContact() {
        if (permissions.all { ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }) {
            saveContact(
                contactProfilePic = convertDrawableResToBitmap(this, R.drawable.ic_launcher_background)
            )
        } else {
            requestMultiplePermissionsLauncher.launch(permissions)
        }
    }

    private fun saveContact(
        numberLabel: Int = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,
        emailLabel: Int = ContactsContract.CommonDataKinds.Email.TYPE_MOBILE,
        birthdayLabel: Int = ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY,
        addressLabel: Int = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME,
        contactProfilePic: Bitmap
    ) {
        if (contactName.isBlank() || contactNumber.isBlank()) {
            Toast.makeText(this, "Please fill the required fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val resolver: ContentResolver = contentResolver

        val values = ContentValues()
        when(SAVE_TO) {
            "Sim" -> {
                values.apply {
                    put(ContactsContract.RawContacts.ACCOUNT_TYPE, "com.android.sim")
                    put(ContactsContract.RawContacts.ACCOUNT_NAME, contactSaveTo)
                }
            }
            "Account" -> {
                values.apply {
                    saveToAccounts(contactSaveTo)?.let {
                        put(ContactsContract.RawContacts.ACCOUNT_TYPE, it.type)
                        put(ContactsContract.RawContacts.ACCOUNT_NAME, it.name)
                    }
                }
            }
        }

        try {
            val rawContactUri: Uri? = resolver.insert(ContactsContract.RawContacts.CONTENT_URI, values)
            val rawContactId: Long = rawContactUri?.lastPathSegment?.toLong() ?: -1

            // Insert Name
            val nameValues = ContentValues().apply {
                put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName)
            }
            resolver.insert(ContactsContract.Data.CONTENT_URI, nameValues)

            // Insert Number
            val phoneValues = ContentValues().apply {
                put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                put(ContactsContract.CommonDataKinds.Phone.NUMBER, contactNumber)
                put(ContactsContract.CommonDataKinds.Phone.TYPE, numberLabel)
            }
            resolver.insert(ContactsContract.Data.CONTENT_URI, phoneValues)

            // Insert Email
            if (contactEmail.isNotBlank()) {
                val emailValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Email.ADDRESS, contactEmail)
                    put(ContactsContract.CommonDataKinds.Email.TYPE, emailLabel)
                }
                resolver.insert(ContactsContract.Data.CONTENT_URI, emailValues)
            }

            // Insert Birthday
            if (contactBirthday.isNotBlank()) {
                val birthdayValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Event.START_DATE, contactBirthday)
                    put(ContactsContract.CommonDataKinds.Event.TYPE, birthdayLabel)
                }
                resolver.insert(ContactsContract.Data.CONTENT_URI, birthdayValues)
            }

            // Insert Address
            if (contactAddress.isNotBlank()) {
                val addressValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.StructuredPostal.CITY, contactAddress)
                    put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, addressLabel)
                }
                resolver.insert(ContactsContract.Data.CONTENT_URI, addressValues)
            }

            // Insert ProfilePic
            if (contactProfilePic.toString().isNotBlank()) {
                val profilePicValues = ContentValues().apply {
                    put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Photo.PHOTO, convertImageToByteArray(contactProfilePic))
                }
                resolver.insert(ContactsContract.Data.CONTENT_URI, profilePicValues)
            }
            Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving contact: $e", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestMultiplePermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                saveContact(contactProfilePic = convertDrawableResToBitmap(this, R.drawable.ic_launcher_background))
            } else {
                Toast.makeText(this, "All permissions are required to save contacts!", Toast.LENGTH_SHORT).show()
            }
        }

    private fun saveToAccounts(accountType: String): Account? {
        val accounts = AccountManager.get(this).getAccountsByType(accountType)
        return if (accounts.isNotEmpty()) {
            Log.d("saveToAccounts", "saveToAccounts: ${accounts[0]}")
            accounts[0]
        } else {
            null
        }
    }

    private fun convertImageToByteArray(image: Bitmap): ByteArray {
        val outPutStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, outPutStream)
        return outPutStream.toByteArray()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun convertDrawableResToBitmap(context: Context, drawableRes: Int): Bitmap {
        val drawable = context.getDrawable(drawableRes)!!
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 1
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 1

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}