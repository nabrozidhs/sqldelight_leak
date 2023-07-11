This project highlights an issue I'm having when using Pager + OffsetQueryPagingSource.

The current implementation of LeakActivity has a memory leak. OffsetQueryPagingSource when used
with Pager does not call the unregister method from sqldelight's android driver when the activity
finishes. This is the method not being called
https://github.com/cashapp/sqldelight/blob/master/drivers/android-driver/src/main/java/app/cash/sqldelight/driver/android/AndroidSqliteDriver.kt#L92.

To test it launch the app, tap the button, and then press back when prompted. LeakCanary should 
eventually detect the leak.