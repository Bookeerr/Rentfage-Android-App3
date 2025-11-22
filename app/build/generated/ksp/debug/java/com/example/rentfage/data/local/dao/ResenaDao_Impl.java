package com.example.rentfage.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.rentfage.data.local.entity.ResenaEntidad;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ResenaDao_Impl implements ResenaDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ResenaEntidad> __insertionAdapterOfResenaEntidad;

  public ResenaDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfResenaEntidad = new EntityInsertionAdapter<ResenaEntidad>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `resenas` (`id`,`userId`,`comentario`,`fechaCreacion`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ResenaEntidad entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getUserId());
        statement.bindString(3, entity.getComentario());
        statement.bindLong(4, entity.getFechaCreacion());
      }
    };
  }

  @Override
  public Object insertar(final ResenaEntidad resena, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfResenaEntidad.insert(resena);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object obtenerResenaPorUserId(final int userId,
      final Continuation<? super ResenaEntidad> $completion) {
    final String _sql = "SELECT * FROM resenas WHERE userId = ? ORDER BY fechaCreacion DESC LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, userId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ResenaEntidad>() {
      @Override
      @Nullable
      public ResenaEntidad call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfComentario = CursorUtil.getColumnIndexOrThrow(_cursor, "comentario");
          final int _cursorIndexOfFechaCreacion = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaCreacion");
          final ResenaEntidad _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpUserId;
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
            final String _tmpComentario;
            _tmpComentario = _cursor.getString(_cursorIndexOfComentario);
            final long _tmpFechaCreacion;
            _tmpFechaCreacion = _cursor.getLong(_cursorIndexOfFechaCreacion);
            _result = new ResenaEntidad(_tmpId,_tmpUserId,_tmpComentario,_tmpFechaCreacion);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ResenaEntidad>> obtenerTodas() {
    final String _sql = "SELECT * FROM resenas ORDER BY fechaCreacion DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"resenas"}, new Callable<List<ResenaEntidad>>() {
      @Override
      @NonNull
      public List<ResenaEntidad> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfComentario = CursorUtil.getColumnIndexOrThrow(_cursor, "comentario");
          final int _cursorIndexOfFechaCreacion = CursorUtil.getColumnIndexOrThrow(_cursor, "fechaCreacion");
          final List<ResenaEntidad> _result = new ArrayList<ResenaEntidad>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ResenaEntidad _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpUserId;
            _tmpUserId = _cursor.getInt(_cursorIndexOfUserId);
            final String _tmpComentario;
            _tmpComentario = _cursor.getString(_cursorIndexOfComentario);
            final long _tmpFechaCreacion;
            _tmpFechaCreacion = _cursor.getLong(_cursorIndexOfFechaCreacion);
            _item = new ResenaEntidad(_tmpId,_tmpUserId,_tmpComentario,_tmpFechaCreacion);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
