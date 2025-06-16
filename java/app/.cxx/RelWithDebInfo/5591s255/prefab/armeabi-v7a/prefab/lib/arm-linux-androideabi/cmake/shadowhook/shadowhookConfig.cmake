if(NOT TARGET shadowhook::shadowhook)
add_library(shadowhook::shadowhook SHARED IMPORTED)
set_target_properties(shadowhook::shadowhook PROPERTIES
    IMPORTED_LOCATION "C:/Users/mtayk/.gradle/caches/8.9/transforms/11fe44731a1634b54b2fbcac14133211/transformed/jetified-shadowhook-1.0.9/prefab/modules/shadowhook/libs/android.armeabi-v7a/libshadowhook.so"
    INTERFACE_INCLUDE_DIRECTORIES "C:/Users/mtayk/.gradle/caches/8.9/transforms/11fe44731a1634b54b2fbcac14133211/transformed/jetified-shadowhook-1.0.9/prefab/modules/shadowhook/include"
    INTERFACE_LINK_LIBRARIES ""
)
endif()

