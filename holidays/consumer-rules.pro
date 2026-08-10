# Firestore/gRPC infra keep rules already ship from the `reviews` module's consumer-rules.pro
# (also always present in `app`), so only this module's own DTOs need keeping here - they're
# reflected into by Firestore's own POJO mapper (DocumentSnapshot.toObject), which is just as
# unsafe under R8 shrinking as Gson reflection if these members aren't kept.
-keepclassmembers class com.github.naz013.holidays.remote.HolidayDocumentDto { *; }
-keep class com.github.naz013.holidays.remote.HolidayDocumentDto { *; }
-keepclassmembers class com.github.naz013.holidays.remote.HolidayItemDto { *; }
-keep class com.github.naz013.holidays.remote.HolidayItemDto { *; }
