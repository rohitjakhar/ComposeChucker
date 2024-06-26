package com.rohitjakhar.composechucker.sample

import android.os.Bundle
import android.os.StrictMode
import android.text.method.LinkMovementMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.rohitjakhar.composechucker.api.Chucker
import com.rohitjakhar.composechucker.api.ChuckerCollector
import com.rohitjakhar.composechucker.api.ExportFormat
import com.rohitjakhar.composechucker.sample.databinding.ActivityMainSampleBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val interceptorTypeSelector = InterceptorTypeSelector()

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainSampleBinding

    private val client by lazy {
        createOkHttpClient(applicationContext, interceptorTypeSelector)
    }

    private val httpTasks by lazy {
        listOf(HttpBinHttpTask(client), DummyImageHttpTask(client), PostmanEchoHttpTask(client))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainSampleBinding.inflate(layoutInflater)

        with(mainBinding) {
            setContentView(root)
            doHttp.setOnClickListener {
                for (task in httpTasks) {
                    task.run()
                }
            }
            doGraphql.setOnClickListener {
                GraphQlTask(client).run()
            }

            launchChuckerDirectly.isVisible = Chucker.isOp
            launchChuckerDirectly.setOnClickListener { launchChuckerDirectly() }

            launchChuckerComposeDirectly?.isVisible = Chucker.isOp
            launchChuckerComposeDirectly?.setOnClickListener { launchChuckerComposeDirectly() }

            exportToFile.isVisible = Chucker.isOp
            exportToFile.setOnClickListener {
                generateExportFile(ExportFormat.LOG)
            }

            exportToFileHar.isVisible = Chucker.isOp
            exportToFileHar.setOnClickListener {
                generateExportFile(ExportFormat.HAR)
            }

            interceptorTypeLabel.movementMethod = LinkMovementMethod.getInstance()
            useApplicationInterceptor.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    interceptorTypeSelector.value = InterceptorType.APPLICATION
                }
            }
            useNetworkInterceptor.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    interceptorTypeSelector.value = InterceptorType.NETWORK
                }
            }
        }

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
    }

    private fun launchChuckerDirectly() {
        // Optionally launch Chucker directly from your own app UI
        startActivity(Chucker.getLaunchIntent(this))
    }

    private fun launchChuckerComposeDirectly() {
        // Optionally launch Chucker directly from your own app UI
        startActivity(Chucker.getComposeLaunchIntent(this))
    }

    private fun generateExportFile(exportFormat: ExportFormat) {
        lifecycleScope.launch {
            val uri = withContext(Dispatchers.IO) {
                ChuckerCollector(this@MainActivity)
                    .writeTransactions(this@MainActivity, null, exportFormat)
            }
            if (uri == null) {
                Toast.makeText(applicationContext, R.string.export_to_file_failure, Toast.LENGTH_SHORT).show()
            } else {
                val successMessage = applicationContext.getString(R.string.export_to_file_success, uri.path)
                Toast.makeText(applicationContext, successMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
