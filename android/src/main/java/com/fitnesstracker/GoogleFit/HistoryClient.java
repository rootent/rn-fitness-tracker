package com.fitnesstracker.GoogleFit;

import android.app.Activity;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

class HistoryClient {
  private final Activity activity;
  private static final String DATE_FORMAT = "yyyy-MM-dd";
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

  HistoryClient(Activity activity) {
    this.activity = activity;
  }

  void getTotalForTimeRange(final Promise promise, long startTime, long endTime, int dataType) {
    try {

      if (dataType == 0) {
        getStepHistory(startTime, endTime, 7, new OnStepsFetch() {
          @Override
          public void onSuccess(int steps) {
            promise.resolve(steps);
          }

          @Override
          public void onFailure(Exception e) {
            promise.reject(e);
          }
        });
      } else if (dataType == 1) {
        getDistanceHistory(startTime, endTime, 7, new OnDistanceFetch() {
          @Override
          public void onSuccess(float distance) {
            promise.resolve(distance);
          }

          @Override
          public void onFailure(Exception e) {
            promise.reject(e);
          }
        });
      }

    } catch (Exception e) {
      promise.reject(e);
      e.printStackTrace();
    }
  }

  void getWeekData(final Promise promise, int dataType) {
    try {
      Date today = new Date();
      long startTime = setMidnight(addDays(today, -7)).getTimeInMillis();
      long now = today.getTime();

      if (dataType == 0) {
        getStepHistory(startTime, now, 7, new OnStepsFetch() {
          @Override
          public void onSuccess(int steps) {
            promise.resolve(steps);
          }

          @Override
          public void onFailure(Exception e) {
            promise.reject(e);
          }
        });
      } else if (dataType == 1) {
        getDistanceHistory(startTime, now, 7, new OnDistanceFetch() {
          @Override
          public void onSuccess(float distance) {
            promise.resolve(distance);
          }

          @Override
          public void onFailure(Exception e) {
            promise.reject(e);
          }
        });
      }

    } catch (Exception e) {
      promise.reject(e);
      e.printStackTrace();
    }
  }

  void getDailyStepsForNumberOfDays(final Date startDate, final Date endDate, final WritableMap stepsData, final Promise promise) {
    try {
      Date start = getStartOfDay(endDate);
      Date end = getEndOfDay(endDate);
      if (formatDate(endDate).equals(formatDate(new Date()))) {
        end = Calendar.getInstance().getTime(); // make sure current day query time is until current time, not end of the day
      }

      getStepHistory(start.getTime(), end.getTime(), 1, new OnStepsFetch() {
        @Override
        public void onSuccess(int steps) {
          if (startDate.getTime() < getEndOfDay(endDate).getTime()) {
            stepsData.putInt(formatDate(endDate), steps);
            Date newEndDate = addDays(endDate, -1);
            getDailyStepsForNumberOfDays(startDate, newEndDate, stepsData, promise);
          } else {
            promise.resolve(stepsData);
          }
        }

        @Override
        public void onFailure(Exception e) {
          promise.reject(e);
        }
      });
    } catch (Exception e) {
      promise.reject(e);
      e.printStackTrace();
    }
  }

  void getDistanceDaily(final Date date, final WritableMap distanceData, final int count, final Promise promise) {
    try {
      Date end = getEndOfDay(date);
      if (count == 0) {
        end = Calendar.getInstance().getTime(); // make sure current day query time is until current time, not end of the day
      }
      Date start = getStartOfDay(date);

      getDistanceHistory(start.getTime(), end.getTime(), 1, new OnDistanceFetch() {
        @Override
        public void onSuccess(float distance) {
          if (count < 7) {
            distanceData.putDouble(formatDate(date), distance);
            Date previousDate = addDays(date, -1);
            getDistanceDaily(previousDate, distanceData, count + 1, promise);
          } else {
            promise.resolve(distanceData);
          }
        }

        @Override
        public void onFailure(Exception e) {
          promise.reject(e);
        }
      });
    } catch (Exception e) {
      promise.reject(e);
      e.printStackTrace();
    }
  }

  void getStepsToday(final Promise promise) {
    try {
      Date today = new Date();
      Date start = getStartOfDay(today);
      Date currentTime = Calendar.getInstance().getTime();

      getStepHistory(start.getTime(), currentTime.getTime(), 1, new OnStepsFetch() {
        @Override
        public void onSuccess(int steps) {
          promise.resolve(steps);
        }

        @Override
        public void onFailure(Exception e) {
          promise.reject(e);
        }
      });
    } catch (Exception e) {
      promise.reject(e);
      e.printStackTrace();
    }
  }

  void getDistanceToday(final Promise promise) {
    try {
      Date today = new Date();
      Date start = getStartOfDay(today);
      Date currentTime = Calendar.getInstance().getTime();

      getDistanceHistory(start.getTime(), currentTime.getTime(), 1, new OnDistanceFetch() {
        @Override
        public void onSuccess(float distance) {
          promise.resolve(distance);
        }

        @Override
        public void onFailure(Exception e) {
          promise.reject(e);
        }
      });
    } catch (Exception e) {
      promise.reject(e);
      e.printStackTrace();
    }
  }


  private void getStepHistory(final long startTime, long endTime, int dayCount, final OnStepsFetch fetchCompleteCallback) {

    DataReadRequest readRequest = new DataReadRequest.Builder()
        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
        .bucketByTime(dayCount, TimeUnit.DAYS)
        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
        .build();

    Fitness.getHistoryClient(this.activity, GoogleSignIn.getLastSignedInAccount(this.activity))
        .readData(readRequest)
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(Exception e) {
            fetchCompleteCallback.onFailure(e);
          }
        })
        .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
          @Override
          public void onComplete(Task<DataReadResponse> task) {
            if (task.isSuccessful()) {
              DataReadResponse response = task.getResult();
              int steps = parseStepsDelta(response);
              fetchCompleteCallback.onSuccess(steps);
            }
          }
        });
  }

  private void getDistanceHistory(final long startTime, long endTime, int dayCount, final OnDistanceFetch fetchCompleteCallback) {

    DataReadRequest readRequest = new DataReadRequest.Builder()
        .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
        .bucketByTime(dayCount, TimeUnit.DAYS)
        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
        .build();

    Fitness.getHistoryClient(this.activity, GoogleSignIn.getLastSignedInAccount(this.activity))
        .readData(readRequest)
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(Exception e) {
            fetchCompleteCallback.onFailure(e);
          }
        })
        .addOnCompleteListener(new OnCompleteListener<DataReadResponse>() {
          @Override
          public void onComplete(Task<DataReadResponse> task) {
            if (task.isSuccessful()) {
              DataReadResponse response = task.getResult();
              float distance = parseDistanceDelta(response);
              fetchCompleteCallback.onSuccess(distance);
            }
          }
        });
  }

  private int parseStepsDelta(DataReadResponse response) {
    List<Bucket> buckets = response.getBuckets();
    int stepCount = 0;
    for (Bucket bucket : buckets) {
      List<DataSet> dataSets = bucket.getDataSets();
      for (DataSet dataSet : dataSets) {
        List<DataPoint> dataPoints = dataSet.getDataPoints();
        for (DataPoint dataPoint : dataPoints) {
          if (dataPoint.getDataType().equals(DataType.TYPE_STEP_COUNT_DELTA)) {
            for (Field field : dataPoint.getDataType().getFields()) {
              if (field.getName().equals("steps")) {
                stepCount += dataPoint.getValue(field).asInt();
              }
            }
          }
        }
      }
    }
    return stepCount;
  }

  private float parseDistanceDelta(DataReadResponse response) {
    List<Bucket> buckets = response.getBuckets();
    float distanceTotal = 0;
    for (Bucket bucket : buckets) {
      List<DataSet> dataSets = bucket.getDataSets();
      for (DataSet dataSet : dataSets) {
        List<DataPoint> dataPoints = dataSet.getDataPoints();
        for (DataPoint dataPoint : dataPoints) {
          if (dataPoint.getDataType().equals(DataType.TYPE_DISTANCE_DELTA)) {
            for (Field field : dataPoint.getDataType().getFields()) {
              distanceTotal += dataPoint.getValue(field).asFloat();
            }
          }
        }
      }
    }
    return distanceTotal;
  }

  private Calendar setMidnight(Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DATE);
    calendar.set(year, month, day, 0, 0, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    return calendar;
  }

  private Date getStartOfDay(Date date) {
    Calendar calendar = setMidnight(date);

    return calendar.getTime();
  }

  private Date getEndOfDay(Date date) {
    Calendar calendar = setMidnight(date);
    calendar.add(Calendar.DATE, 1);

    return calendar.getTime();
  }

  private Date addDays(Date date, int daysDifference) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    cal.add(Calendar.DATE, daysDifference);
    Date finalDate = cal.getTime();
    return getStartOfDay(finalDate);
  }

  private String formatDate(Date date) {
    return dateFormat.format(date);
  }

}

interface OnStepsFetch {
  void onSuccess(int steps);

  void onFailure(Exception e);
}

interface OnDistanceFetch {
  void onSuccess(float steps);

  void onFailure(Exception e);
}
