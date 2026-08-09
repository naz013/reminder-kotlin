# Firestore/gRPC infra keep rules already ship from the `reviews` module's consumer-rules.pro
# (also always present in `app`), so only this module's own Gson-reflected DTOs need keeping here -
# see CLAUDE.md: unannotated Gson-reflected fields are not R8-safe.
-keepclassmembers class com.github.naz013.holidays.remote.HolidayDocumentDto { *; }
-keep class com.github.naz013.holidays.remote.HolidayDocumentDto { *; }
-keepclassmembers class com.github.naz013.holidays.remote.HolidayItemDto { *; }
-keep class com.github.naz013.holidays.remote.HolidayItemDto { *; }
