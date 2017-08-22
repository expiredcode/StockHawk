package com.udacity.stockhawk;

import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.ui.StockDetailActivity;

/**
 * Created by Vale on 20/05/2017.
 */

public class QuoteWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor data = null;
            @Override
            public void onCreate() {
                // do nothing
            }

            @Override
            public void onDataSetChanged() {
                if(data!=null){
                    data.close();
                }
                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(Contract.Quote.URI, new String[]{},null,null,null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if(data!=null){
                    data.close();
                    data=null;
                }

            }

            @Override
            public int getCount() {
                return data==null?0:data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                RemoteViews views = new RemoteViews(getPackageName(),R.layout.widget_collection_item);
                if(data.moveToPosition(position)){
                    views.setTextViewText(R.id.stock_symbol, data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)));
                    views.setTextViewText(R.id.stock_value,"$"+ data.getString(data.getColumnIndex(Contract.Quote.COLUMN_PRICE)));
                    String s = data.getString(data.getColumnIndex(Contract.Quote.COLUMN_PERCENTAGE_CHANGE));

                    if(s.contains("-")) {
                        views.setImageViewResource(R.id.stock_arrow,R.drawable.stock_index_down );
                    }else{
                        views.setTextViewText(R.id.stock_history,"+"+s);
                        views.setImageViewResource(R.id.stock_arrow,R.drawable.stock_index_up );
                    }

                    final Intent fillInIntent = new Intent();
                    fillInIntent.putExtra(Intent.EXTRA_TEXT,data.getString(data.getColumnIndex(Contract.Quote.COLUMN_SYMBOL)));
                    views.setOnClickFillInIntent(R.id.widget_list_element, fillInIntent);
                }

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if(data!=null&& data.moveToPosition(position)) {
                    return data.getLong(Contract.Quote.POSITION_ID);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }
        };
    }
}
