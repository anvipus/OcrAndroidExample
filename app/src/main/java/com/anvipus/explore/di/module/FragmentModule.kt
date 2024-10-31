package com.anvipus.explore.di.module

import com.anvipus.explore.ui.xml.MainFragment
import com.anvipus.explore.ui.xml.ocr.CaptureKTPOcrFragment
import com.anvipus.explore.ui.xml.ocr.OCRResultFragment
import com.anvipus.explore.ui.xml.ocr.ScanOCRFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Suppress("unused")
@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun main(): MainFragment

    @ContributesAndroidInjector
    abstract fun scanKtp(): ScanOCRFragment

    @ContributesAndroidInjector
    abstract fun scanKtpResult(): OCRResultFragment

    @ContributesAndroidInjector
    abstract fun captureKtp(): CaptureKTPOcrFragment

}