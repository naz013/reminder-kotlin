# Firestore/gRPC infra keep rules already ship from the `reviews` module's consumer-rules.pro
# (also always present in `app`). This module reads holiday documents as raw
# Map<String, Any?>/List<Any?> (see HolidayItemMapper.kt) rather than reflecting into a custom
# POJO/DTO class, so no additional -keep rules are needed here.
