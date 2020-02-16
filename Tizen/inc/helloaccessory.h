#ifndef __HELLO_ACCESSORY_PROVIDER_H__
#define __HELLO_ACCESSORY_PROVIDER_H__

#include <app.h>
#include <Elementary.h>
#include <system_settings.h>
#include <efl_extension.h>
#include <dlog.h>

#define TAG "MotionUnlock"

#define NUM_OF_ITEMS 5

void initialize_sap();
void update_ui(char *data);

#if !defined(PACKAGE)
#define PACKAGE "com.gpillusion.motionunlock"
#endif

#endif
