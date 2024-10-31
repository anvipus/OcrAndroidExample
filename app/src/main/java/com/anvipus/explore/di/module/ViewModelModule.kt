package com.anvipus.explore.di.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anvipus.explore.di.AppViewModelFactory
import com.anvipus.explore.ui.xml.MainViewModel
import com.anvipus.explore.ui.xml.ocr.CaptureOCRViewModel
import com.anvipus.explore.ui.xml.ocr.ConfirmationOCRViewModel
import com.codedisruptors.dabestofme.di.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    abstract fun bindViewModelFactory(factory: AppViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(mainViewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CaptureOCRViewModel::class)
    abstract fun bindCaptureOCRViewModel(captureOCRViewModel: CaptureOCRViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ConfirmationOCRViewModel::class)
    abstract fun bindConfirmationOCRViewModel(confirmationOCRViewModel: ConfirmationOCRViewModel): ViewModel


}