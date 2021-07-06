package com.toyamasnsn.andexp

import android.Manifest
import android.content.ContentProviderOperation
import android.database.Cursor
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.database.getStringOrNull
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.ktx.constructPermissionsRequest


class MainActivity : AppCompatActivity() {

    private fun log(s: String) {
        Log.d("MAIN", s)
    }

    private val showContactPermission = constructPermissionsRequest(
        permissions = arrayOf(
//            Manifest.permission.READ_CONTACTS,
//            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
        ),
        onShowRationale = ::onContactShowRationale,
        onPermissionDenied = ::onContactDenied,
        onNeverAskAgain = ::onContactNeverAskAgain
    ) {
        log("aaa")
    }

    private fun onContactDenied() {
        log("denied")
    }

    private fun onContactShowRationale(request: PermissionRequest) {
        request.proceed()
    }

    private fun onContactNeverAskAgain() {
        log("never ask")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showContactPermission.launch()

        findViewById<View>(R.id.applyBatch).setOnClickListener {
//            applyBatch()
        }

        findViewById<View>(R.id.load).setOnClickListener {
//            load()
            loadCalls()
        }
    }

    private fun load() {
        val contactUri = ContactsContract.Data.CONTENT_URI
        val projection: Array<String> = arrayOf(
            ContactsContract.Contacts.Entity.RAW_CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
            ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME,
        )
        val sortOrder = "${ContactsContract.Contacts.Entity.RAW_CONTACT_ID} ASC"
        contentResolver.query(contactUri, projection, null, null, sortOrder)?.use { c ->
            if (c.moveToFirst().not()) {
                log("failed to move to first")
                return@use
            }

            while (c.moveToNext()) {
                val contactId = c.str(ContactsContract.Contacts.Entity.RAW_CONTACT_ID)
                val number = c.str(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val type = c.int(ContactsContract.CommonDataKinds.Phone.TYPE)
                val displayName = c.str(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val displayPrimary =
                    c.str(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
                val givenName = c.str(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
                val familyName = c.str(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)
                val nameDisplayName =
                    c.str(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME)
                val middleName = c.str(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)
                c
            }


        }
            ?: run {
                log("Cusror = null")
            }
    }

    private fun Cursor.str(s: String) = this.getStringOrNull(getColumnIndexOrThrow(s))
    private fun Cursor.int(s: String) = this.getInt(getColumnIndexOrThrow(s))

    // 注:1 つの未加工連絡先を変更する場合は、変更を独自アプリ内で処理するのではなく、インテントを端末の連絡先アプリに送信することを検討してください。
    // この方法については、インテントを使用した取得と変更で詳しく説明しています。
    private fun applyBatch() {
        // Creates a new array of ContentProviderOperation objects.
        val ops = arrayListOf<ContentProviderOperation>()

        /*
         * Creates a new raw contact with its account type (server type) and account name
         * (user's account). Remember that the display name is not stored in this row, but in a
         * StructuredName data row. No other data is required.
         */
        var op: ContentProviderOperation.Builder =
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "com.toyamasnsn.andexp")
                .withValue(
                    ContactsContract.RawContacts.ACCOUNT_NAME,
                    "yamada@andexp.htoyamasnsn.com"
                )
//        ops.add(op.build())

        op = ContentProviderOperation.newInsert(ContactsContract.Settings.CONTENT_URI)
//            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, "com.toyamasnsn.andexp")
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "yamada@andexp.htoyamasnsn.com")
            .withValue(ContactsContract.Settings.UNGROUPED_VISIBLE, 0)
//            .withValue(
//                ContactsContract.CommonDataKinds.GroupMembership.GROUP_SOURCE_ID,
//                "group_source_id1"
//            )
        ops.add(op.build())

//        op = ContentProviderOperation.newInsert(ContactsContract.Settings.CONTENT_URI)
//            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//            .withValue(ContactsContract.Settings.UNGROUPED_VISIBLE, 0)
//        ops.add(op.build())

        // Creates the display name for the new raw contact, as a StructuredName data row.
        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            /*
             * Sets the value of the raw contact id column to the new raw contact ID returned
             * by the first operation in the batch.
             */
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

            // Sets the data row's MIME type to Phone
            .withValue(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
            )
//            .withValue(ContactsContract.Data.DISPLAY_NAME, "yamada taro")
//            .withValue(ContactsContract.CommonDataKinds.Phone., "yamada taro")
            // Sets the phone number and type
            .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, "09011111113")
            .withValue(
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
            )
        // これがアトミックの単位
//            .withYieldAllowed()
        ops.add(op.build())

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            // name
            .withValue(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.MIMETYPE
            )
            .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, "FamilyName2")
            .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, "GivenName2")
        ops.add(op.build())

//        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
//            .withValue(
//                ContactsContract.Data.MIMETYPE,
//                ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE
//            )
//            .withValue(
//                ContactsContract.CommonDataKinds.GroupMembership.GROUP_SOURCE_ID,
//                "group_source_id1"
//            )
////            .withValue(ContactsContract.Settings.UNGROUPED_VISIBLE, 1)
//        ops.add(op.build())

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        } catch (e: Exception) {
            // Display a warning
//            Toast.makeText(applicationContext, "failed to apply batch", Toast.LENGTH_SHORT).show()
            Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private fun loadCalls() {
        val uri = CallLog.Calls.CONTENT_URI
//        val projection = arrayOf(
//            CallLog.Calls.NUMBER,
//            CallLog.Calls.TYPE,
//            CallLog.Calls.COUNTRY_ISO,
//        )
        contentResolver.query(uri, null, null, null, null)?.use { c ->
//        contentResolver.query(uri, projection, null, null, null)?.use { c ->
            c.moveToFirst()
            while (c.moveToNext()) {
                val number = c.str(CallLog.Calls.NUMBER)
                val type = c.int(CallLog.Calls.TYPE)
                val name = c.str(CallLog.Calls.CACHED_NAME)
                val location = c.str(CallLog.Calls.GEOCODED_LOCATION)
                val country = c.str(CallLog.Calls.COUNTRY_ISO)
                log("number = $number, type = $type, name = $name, country = $country, location = $location")
            }
        }
    }

}