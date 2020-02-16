/*
 * Copyright (c) 2015 Samsung Electronics Co., Ltd All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include "main_app.h"
#include "helloaccessory.h"
#include <sensor.h>
#include <sap.h>
#define BUFLEN 200

// UI 구성 요소
Evas_Object *GLOBAL_DEBUG_BOX;
Evas_Object *event_label; // 버튼 사이 공백
Evas_Object *start, *stop; // START, STOP 버튼
Evas_Object *conform;

sensor_listener_h listener0; // 심박수 센서(HRM)
sensor_listener_h listener1; // 가속도 센서(ACC)
sensor_listener_h listener2; // 자이로 센서(GYR)
sensor_listener_h listener3; // 압력 센서(PRS)

float HRM = 0;
float ACC_X = 0;
float ACC_Y = 0;
float ACC_Z = 0;
float GYR_X = 0;
float GYR_Y = 0;
float GYR_Z = 0;
float PRS = 0;

void on_sensor_event(sensor_h sensor, sensor_event_s *event, void *user_data)
{
    // Select a specific sensor with a sensor handle
    sensor_type_e type;
    sensor_get_type(sensor, &type);

    switch (type) {
    case SENSOR_HRM:
    	HRM = event->values[0];
    	dlog_print(DLOG_DEBUG, LOG_TAG, "HRM: %.1f", HRM);
    	break;
    case SENSOR_ACCELEROMETER:
    	ACC_X = event->values[0];
    	ACC_Y = event->values[1];
    	ACC_Z = event->values[2];
    	dlog_print(DLOG_DEBUG, LOG_TAG, "ACC_X: %.1f", ACC_X);
    	dlog_print(DLOG_DEBUG, LOG_TAG, "ACC_Y: %.1f", ACC_Y);
    	dlog_print(DLOG_DEBUG, LOG_TAG, "ACC_Z: %.1f", ACC_Z);
    	break;
    case SENSOR_GYROSCOPE:
    	GYR_X = event->values[0];
    	GYR_Y = event->values[1];
    	GYR_Z = event->values[2];
    	dlog_print(DLOG_DEBUG, LOG_TAG, "GYR_X: %.1f", GYR_X);
    	dlog_print(DLOG_DEBUG, LOG_TAG, "GYR_Y: %.1f", GYR_Y);
    	dlog_print(DLOG_DEBUG, LOG_TAG, "GYR_Z: %.1f", GYR_Z);
    	break;
    case SENSOR_PRESSURE:
    	PRS = event->values[0];
    	dlog_print(DLOG_DEBUG, LOG_TAG, "PRS: %.1f", PRS);
    	break;
    default:
        dlog_print(DLOG_ERROR, LOG_TAG, "Not a relevant event");
    }
}

void _sensor_accuracy_changed_cb(sensor_h sensor, unsigned long long timestamp,
                                 sensor_data_accuracy_e accuracy, void *data)
{
    dlog_print(DLOG_DEBUG, LOG_TAG, "Sensor accuracy change callback invoked");
}

void _sensor_start_cb(void *data, Evas_Object *obj, void *event_info)
{
    void *user_data = NULL;

    // Retrieving a Sensor
    sensor_type_e type0 = SENSOR_HRM;
    sensor_type_e type1 = SENSOR_ACCELEROMETER;
    sensor_type_e type2 = SENSOR_GYROSCOPE;
    sensor_type_e type3 = SENSOR_PRESSURE;

    sensor_h sensor0;
    sensor_h sensor1;
    sensor_h sensor2;
    sensor_h sensor3;

    sensor_get_default_sensor(type0, &sensor0);
    sensor_get_default_sensor(type1, &sensor1);
    sensor_get_default_sensor(type2, &sensor2);
    sensor_get_default_sensor(type3, &sensor3);

    sensor_create_listener(sensor0, &listener0);
    sensor_create_listener(sensor1, &listener1);
    sensor_create_listener(sensor2, &listener2);
    sensor_create_listener(sensor3, &listener3);

    //                                      | 이 숫자가 ms.
    sensor_listener_set_event_cb(listener0, 20, on_sensor_event, user_data);
    sensor_listener_set_event_cb(listener1, 20, on_sensor_event, user_data);
    sensor_listener_set_event_cb(listener2, 20, on_sensor_event, user_data);
    sensor_listener_set_event_cb(listener3, 20, on_sensor_event, user_data);

    sensor_listener_start(listener0);
    sensor_listener_start(listener1);
    sensor_listener_start(listener2);
    sensor_listener_start(listener3);

    elm_object_disabled_set(start, EINA_TRUE);
    elm_object_disabled_set(stop, EINA_FALSE);

    /*
    bool supported;
    int hrm = sensor_is_supported(type, &supported);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_is_supported error: %d", hrm);
        return;
    }

    if (supported) {
    	dlog_print(DLOG_DEBUG, LOG_TAG, "HRM is%s supported", supported ? "" : " not");
    	sprintf(out,"HRM is%s supported", supported ? "" : " not");
    	elm_object_text_set(event_label, out);
    }

    // Get sensor list
    int count;
    sensor_h *list;

    hrm = sensor_get_sensor_list(type, &list, &count);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_get_sensor_list error: %d", hrm);
    } else {
        dlog_print(DLOG_DEBUG, LOG_TAG, "Number of sensors: %d", count);
        free(list);
    }

    hrm = sensor_get_default_sensor(type, &sensor);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_get_default_sensor error: %d", hrm);
        return;
    }

    dlog_print(DLOG_DEBUG, LOG_TAG, "sensor_get_default_sensor");

    // Registering a Sensor Event
    hrm = sensor_create_listener(sensor, &listener);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_create_listener error: %d", hrm);
        return;
    }

    dlog_print(DLOG_DEBUG, LOG_TAG, "sensor_create_listener");

    int min_interval = 0;
    hrm = sensor_get_min_interval(sensor, &min_interval);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_get_min_interval error: %d", hrm);
        return;
    }

    dlog_print(DLOG_DEBUG, LOG_TAG, "Minimum interval of the sensor: %d", min_interval);


    // Callback for sensor value change
    hrm = sensor_listener_set_event_cb(listener, min_interval, on_sensor_event, user_data);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_listener_set_event_cb error: %d", hrm);
        return;
    }

    dlog_print(DLOG_DEBUG, LOG_TAG, "sensor_listener_set_event_cb");

    // Registering the Accuracy Changed Callback
    hrm = sensor_listener_set_accuracy_cb(listener, _sensor_accuracy_changed_cb, user_data);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_listener_set_accuracy_cb error: %d", hrm);
        return;
    }

    dlog_print(DLOG_DEBUG, LOG_TAG, "sensor_listener_set_accuracy_cb");

    hrm = sensor_listener_set_interval(listener, 100);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_listener_set_interval error: %d", hrm);
        return;
    }

    dlog_print(DLOG_DEBUG, LOG_TAG, "sensor_listener_set_intervals");

    hrm = sensor_listener_set_option(listener, SENSOR_OPTION_ALWAYS_ON);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_listener_set_option error: %d", hrm);
        return;
    }

    dlog_print(DLOG_DEBUG, LOG_TAG, "sensor_listener_set_option");

    hrm = sensor_listener_start(listener);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_listener_start error: %d", hrm);
        return;
    }

    dlog_print(DLOG_DEBUG, LOG_TAG, "sensor_listener_start");

    sensor_event_s event;
    hrm = sensor_listener_read_data(listener, &event);
    if (hrm != SENSOR_ERROR_NONE) {

        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_listener_read_data error: %d", hrm);
        return;
    }

    switch (type) {
    	case SENSOR_HRM:
    		dlog_print(DLOG_INFO, LOG_TAG, "HRM: %.1f" , event.values[0]);
    		sprintf(out,"%f", event.values[0]);
    		elm_object_text_set(event_label, out);
    		break;
    	case SENSOR_ACCELEROMETER:
    		dlog_print(DLOG_INFO, LOG_TAG, "ACC ==> X: %.1f, Y: %.1f, Z: %.1f", event.values[0], event.values[1], event.values[2]);
    		break;
    	default:
    		dlog_print(DLOG_ERROR, LOG_TAG, "Not an HRM event");
    }

    dlog_print(DLOG_DEBUG, LOG_TAG, out);

    char *name = NULL;
    char *vendor = NULL;
    float min_range = 0.0;
    float max_range = 220.0;
    float resolution = 0.0;

    hrm = sensor_get_name(sensor, &name);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_get_name error: %d", hrm);
        return;
    }

    dlog_print(DLOG_DEBUG, LOG_TAG, "Sensor name: %s", name);
    free(name);

    hrm = sensor_get_vendor(sensor, &vendor);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_get_vendor error: %d", hrm);
        return;
    }


    dlog_print(DLOG_DEBUG, LOG_TAG, "Sensor vendor: %s", vendor);
    free(vendor);

    hrm = sensor_get_type(sensor, &type);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_get_type error: %d", hrm);
        return;
    }

    dlog_print(DLOG_DEBUG, LOG_TAG, "Sensor type: %s",
            type == SENSOR_ACCELEROMETER               ? "Accelerometer"
          : type == SENSOR_GRAVITY                     ? "Gravity sensor"
          : type == SENSOR_LINEAR_ACCELERATION         ? "Linear acceleration sensor"
          : type == SENSOR_MAGNETIC                    ? "Magnetic sensor"
          : type == SENSOR_ROTATION_VECTOR             ? "Rotation Vector sensor"
          : type == SENSOR_ORIENTATION                 ? "Orientation sensor"
          : type == SENSOR_GYROSCOPE                   ? "Gyroscope sensor"
          : type == SENSOR_LIGHT                       ? "Light sensor"
          : type == SENSOR_PROXIMITY                   ? "Proximity sensor"
          : type == SENSOR_PRESSURE                    ? "Pressure sensor"
          : type == SENSOR_ULTRAVIOLET                 ? "Ultraviolet sensor"
          : type == SENSOR_TEMPERATURE                 ? "Temperature sensor"
          : type == SENSOR_HUMIDITY                    ? "Humidity sensor"
          : type == SENSOR_HRM                         ? "Heart Rate Monitor sensor (Since Tizen 2.3.1)"
          : type == SENSOR_HRM_LED_GREEN               ? "HRM (LED Green) sensor (Since Tizen 2.3.1)"
          : type == SENSOR_HRM_LED_IR                  ? "HRM (LED IR) sensor (Since Tizen 2.3.1)"
          : type == SENSOR_HRM_LED_RED                 ? "HRM (LED RED) sensor (Since Tizen 2.3.1)"
          : type == SENSOR_LAST                        ? "End of sensor enum values" : "Custom sensor");

    hrm = sensor_get_min_range(sensor, &min_range);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_get_min_range error: %d", hrm);
        return;
    }

    dlog_print(DLOG_DEBUG, LOG_TAG, "Minimum range of the sensor: %f", min_range);

    hrm = sensor_get_max_range(sensor, &max_range);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_get_max_range error: %d", hrm);
        return;
    }

    dlog_print(DLOG_DEBUG, LOG_TAG, "Maximum range of the sensor: %f", max_range);

    hrm = sensor_get_resolution(sensor, &resolution);
    if (hrm != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_get_resolution error: %d", hrm);
        return;
    }

    dlog_print(DLOG_DEBUG, LOG_TAG, "Resolution of the sensor: %f", resolution);

    elm_object_disabled_set(start, EINA_TRUE);
    elm_object_disabled_set(stop, EINA_FALSE);
    */
}

void _sensor_stop_cb(void *data, Evas_Object *obj, void *event_info)
{
	/*
    int error = sensor_listener_unset_event_cb(listener);
    if (error != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_listener_unset_event_cb error: %d", error);
    }

    error = sensor_listener_stop(listener);
    if (error != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_listener_stop error: %d", error);
    }

    error = sensor_destroy_listener(listener);
    if (error != SENSOR_ERROR_NONE) {
        dlog_print(DLOG_ERROR, LOG_TAG, "sensor_destroy_listener error: %d", error);
    }
    */

	sensor_listener_unset_event_cb(listener0);
	sensor_listener_stop(listener0);
	sensor_destroy_listener(listener0);

	sensor_listener_unset_event_cb(listener1);
	sensor_listener_stop(listener1);
	sensor_destroy_listener(listener1);

	sensor_listener_unset_event_cb(listener2);
	sensor_listener_stop(listener2);
	sensor_destroy_listener(listener2);

	sensor_listener_unset_event_cb(listener3);
	sensor_listener_stop(listener3);
	sensor_destroy_listener(listener3);

    elm_object_disabled_set(start, EINA_FALSE);
    elm_object_disabled_set(stop, EINA_TRUE);
}

void _add_entry_text(const char *text)
{
    Evas_Coord c_y;

    elm_entry_entry_append(GLOBAL_DEBUG_BOX, text);
    elm_entry_entry_append(GLOBAL_DEBUG_BOX, "<br>");
    elm_entry_cursor_end_set(GLOBAL_DEBUG_BOX);
    elm_entry_cursor_geometry_get(GLOBAL_DEBUG_BOX, NULL, &c_y, NULL, NULL);
    elm_scroller_region_show(GLOBAL_DEBUG_BOX, 0, c_y, 0, 0);
}

static void win_delete_request_cb(void *data, Evas_Object *obj, void *event_info)
{
    elm_exit();
}

Eina_Bool _pop_cb(void *data, Elm_Object_Item *item)
{
    elm_win_lower(((appdata_s *)data)->win);
    return EINA_FALSE;
}

Evas_Object *_new_button(appdata_s *ad, Evas_Object *display, char *name, void *cb)
{
    // Create a button
    Evas_Object *bt = elm_button_add(display);
    elm_object_text_set(bt, name);
    evas_object_smart_callback_add(bt, "clicked", (Evas_Smart_Cb) cb, ad);
    evas_object_size_hint_weight_set(bt, EVAS_HINT_EXPAND, 0.0);
    evas_object_size_hint_align_set(bt, EVAS_HINT_FILL, EVAS_HINT_FILL);
    elm_box_pack_end(display, bt);
    evas_object_show(bt);
    return bt;
}


void _create_new_cd_display(appdata_s *ad, char *name, void *cb) {
    // Create main box
    Evas_Object *box = elm_box_add(conform);
    elm_object_content_set(conform, box);
    elm_box_horizontal_set(box, EINA_FALSE);
    evas_object_size_hint_align_set(box, EVAS_HINT_FILL, EVAS_HINT_FILL);
    evas_object_size_hint_weight_set(box, EVAS_HINT_EXPAND, EVAS_HINT_EXPAND);
    evas_object_show(box);

    start = _new_button(ad, box, "Start", _sensor_start_cb);
    event_label = elm_label_add(box);
    elm_object_text_set(event_label, " ");
    elm_box_pack_end(box, event_label);
    evas_object_show(event_label);
    stop = _new_button(ad, box, "Stop", _sensor_stop_cb);
}

static void create_base_gui(appdata_s *ad) {
    // Setting the window
    ad->win = elm_win_util_standard_add(PACKAGE, PACKAGE);
    elm_win_conformant_set(ad->win, EINA_TRUE);
    elm_win_autodel_set(ad->win, EINA_TRUE);
    elm_win_indicator_mode_set(ad->win, ELM_WIN_INDICATOR_SHOW);
    elm_win_indicator_opacity_set(ad->win, ELM_WIN_INDICATOR_OPAQUE);
    evas_object_smart_callback_add(ad->win, "delete, request", win_delete_request_cb, NULL);

    /* Create conformant */
    conform = elm_conformant_add(ad->win);

    evas_object_size_hint_weight_set(conform, EVAS_HINT_EXPAND, EVAS_HINT_EXPAND);
    elm_win_resize_object_add(ad->win, conform);
    evas_object_show(conform);

    // Create a naviframe
    ad->navi = elm_naviframe_add(conform);
    evas_object_size_hint_align_set(ad->navi, EVAS_HINT_FILL, EVAS_HINT_FILL);
    evas_object_size_hint_weight_set(ad->navi, EVAS_HINT_EXPAND, EVAS_HINT_EXPAND);

    elm_object_content_set(conform, ad->navi);
    evas_object_show(ad->navi);

    // Fill the list with items
    //create_buttons_in_main_window(ad);
    _create_new_cd_display(ad, "Sensor", _pop_cb);

    eext_object_event_callback_add(ad->navi, EEXT_CALLBACK_BACK, eext_naviframe_back_cb, NULL);

    // Show the window after base gui is set up
    evas_object_show(ad->win);
}

static bool app_create(void *data) {
    /*
     * Hook to take necessary actions before main event loop starts
     * Initialize UI resources and application's data
     * If this function returns true, the main loop of application starts
     * If this function returns false, the application is terminated
     */
    create_base_gui((appdata_s *)data);
    initialize_sap();

    return true;
}

int main(int argc, char *argv[]) {
    appdata_s ad;
    memset(&ad, 0x00, sizeof(appdata_s));

    ui_app_lifecycle_callback_s event_callback;
    memset(&event_callback, 0x00, sizeof(ui_app_lifecycle_callback_s));

    event_callback.create = app_create;

    int ret = ui_app_main(argc, argv, &event_callback, &ad);
    if (ret != APP_ERROR_NONE)
        dlog_print(DLOG_ERROR, LOG_TAG, "ui_app_main() failed with error: %d", ret);

    return ret;
}
