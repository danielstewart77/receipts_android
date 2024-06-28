package com.sparktobloom.receipts.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sparktobloom.receipts.repository.SparkRepository
import java.lang.reflect.InvocationTargetException

class ViewModelFactory<T : ViewModel>(
    private val viewModelClass: Class<T>,
    private val sparkRepo: SparkRepository,
    private val application: Application,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        Log.d("ViewModelFactory", "Creating ViewModel for: ${modelClass.simpleName}")
        return try {
            viewModelClass.getConstructor(SparkRepository::class.java, Application::class.java)
                .newInstance(sparkRepo, application) as T
        } catch (e: NoSuchMethodException) {
            throw IllegalArgumentException("Unknown ViewModel class", e)
        } catch (e: IllegalAccessException) {
            throw IllegalArgumentException("Unknown ViewModel class", e)
        } catch (e: InstantiationException) {
            throw IllegalArgumentException("Unknown ViewModel class", e)
        } catch (e: InvocationTargetException) {
            throw IllegalArgumentException("Unknown ViewModel class", e)
        }
    }
}
