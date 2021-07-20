package org.odk.collect.android.configure.qr

import android.content.Context
import com.google.zxing.integration.android.IntentIntegrator
import com.journeyapps.barcodescanner.BarcodeResult
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.activities.MainMenuActivity
import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.configure.SettingsImporter
import org.odk.collect.android.fragments.BarCodeScannerFragment
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.utilities.CompressionUtils
import org.odk.collect.android.utilities.ToastUtils.showLongToast
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.zip.DataFormatException
import javax.inject.Inject

class QRCodeScannerFragment : BarCodeScannerFragment() {
    @Inject
    lateinit var settingsImporter: SettingsImporter

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var currentProjectProvider: CurrentProjectProvider

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    @Throws(IOException::class, DataFormatException::class)
    override fun handleScanningResult(result: BarcodeResult) {
        val importSuccess = settingsImporter.fromJSON(
            CompressionUtils.decompress(result.text),
            currentProjectProvider.getCurrentProject()
        )
        val settingsHash = getMd5Hash(ByteArrayInputStream(result.text.toByteArray()))
        if (importSuccess) {
            showLongToast(getString(R.string.successfully_imported_settings))
            analytics.logEvent(AnalyticsEvents.SETTINGS_IMPORT_QR, "Success", settingsHash!!)
            ActivityUtils.startActivityAndCloseAllOthers(
                requireActivity(),
                MainMenuActivity::class.java
            )
        } else {
            showLongToast(getString(R.string.invalid_qrcode))
            analytics.logEvent(
                AnalyticsEvents.SETTINGS_IMPORT_QR,
                "No valid settings",
                settingsHash!!
            )
        }
    }

    override fun getSupportedCodeFormats(): Collection<String> {
        return listOf(IntentIntegrator.QR_CODE)
    }
}
