package pl.soulsnaps.components

import androidx.compose.runtime.Composable
import platform.Foundation.*
import platform.UIKit.*

@Composable
actual fun showPlatformDatePicker(
    initialDateMillis: Long?,
    onDateSelected: (Long) -> Unit
) {
    val datePicker = UIDatePicker().apply {
        datePickerMode = UIDatePickerMode.UIDatePickerModeDate
        preferredDatePickerStyle = UIDatePickerStyle.UIDatePickerStyleWheels
        initialDateMillis?.let {
            date = NSDate.dateWithTimeIntervalSince1970(it.toDouble() / 1000.0)
        }
    }

    val alertController = UIAlertController.alertControllerWithTitle(
        title = "Pick a date",
        message = "\n\n\n\n\n\n\n\n\n",
        preferredStyle = UIAlertControllerStyleAlert
    )

    alertController.view.addSubview(datePicker)

    val okAction = UIAlertAction.actionWithTitle(
        title = "OK",
        style = UIAlertActionStyleDefault
    ) {
        val selectedDateMillis = (datePicker.date.timeIntervalSince1970 * 1000).toLong()
        onDateSelected(selectedDateMillis)
    }

    val cancelAction = UIAlertAction.actionWithTitle(
        title = "Cancel",
        style = UIAlertActionStyleCancel,
        handler = null
    )

    alertController.addAction(okAction)
    alertController.addAction(cancelAction)

    val window = UIApplication.sharedApplication.keyWindow
    val rootVC = window?.rootViewController
    rootVC?.presentViewController(alertController, animated = true, completion = null)
}
