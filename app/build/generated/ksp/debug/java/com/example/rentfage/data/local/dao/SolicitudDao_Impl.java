package com.example.rentfage.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.rentfage.data.local.entity.SolicitudEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
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
public final class SolicitudDao_Impl implements SolicitudDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SolicitudEntity> __insertionAdapterOfSolicitudEntity;

  private final SharedSQLiteStatement __preparedStmtOfActualizarEstado;

  private final SharedSQLiteStatement __preparedStmtOfBorrarPorUsuario;

  private final SharedSQLiteStatement __preparedStmtOfBorrarTodas;

  public SolicitudDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSolicitudEntity = new EntityInsertionAdapter<SolicitudEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `solicitudes` (`id`,`usuarioId`,`usuarioEmail`,`casaId`,`fecha`,`estado`,`tituloPropiedad`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SolicitudEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getUsuarioId());
        statement.bindString(3, entity.getUsuarioEmail());
        statement.bindLong(4, entity.getCasaId());
        statement.bindString(5, entity.getFecha());
        statement.bindString(6, entity.getEstado());
        if (entity.getTituloPropiedad() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getTituloPropiedad());
        }
      }
    };
    this.__preparedStmtOfActualizarEstado = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE solicitudes SET estado = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfBorrarPorUsuario = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM solicitudes WHERE usuarioEmail = ?";
        return _query;
      }
    };
    this.__preparedStmtOfBorrarTodas = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM solicitudes";
        return _query;
      }
    };
  }

  @Override
  public Object upsert(final SolicitudEntity solicitud,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfSolicitudEntity.insertAndReturnId(solicitud);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertAll(final List<SolicitudEntity> solicitudes,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSolicitudEntity.insert(solicitudes);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object actualizarEstado(final int id, final String nuevoEstado,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfActualizarEstado.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, nuevoEstado);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfActualizarEstado.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object borrarPorUsuario(final String email, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfBorrarPorUsuario.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, email);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfBorrarPorUsuario.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object borrarTodas(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfBorrarTodas.acquire();
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfBorrarTodas.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SolicitudEntity>> obtenerPorUsuario(final String email) {
    final String _sql = "SELECT * FROM solicitudes WHERE usuarioEmail = ? ORDER BY fecha DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, email);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"solicitudes"}, new Callable<List<SolicitudEntity>>() {
      @Override
      @NonNull
      public List<SolicitudEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUsuarioId = CursorUtil.getColumnIndexOrThrow(_cursor, "usuarioId");
          final int _cursorIndexOfUsuarioEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "usuarioEmail");
          final int _cursorIndexOfCasaId = CursorUtil.getColumnIndexOrThrow(_cursor, "casaId");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfTituloPropiedad = CursorUtil.getColumnIndexOrThrow(_cursor, "tituloPropiedad");
          final List<SolicitudEntity> _result = new ArrayList<SolicitudEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SolicitudEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final long _tmpUsuarioId;
            _tmpUsuarioId = _cursor.getLong(_cursorIndexOfUsuarioId);
            final String _tmpUsuarioEmail;
            _tmpUsuarioEmail = _cursor.getString(_cursorIndexOfUsuarioEmail);
            final int _tmpCasaId;
            _tmpCasaId = _cursor.getInt(_cursorIndexOfCasaId);
            final String _tmpFecha;
            _tmpFecha = _cursor.getString(_cursorIndexOfFecha);
            final String _tmpEstado;
            _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            final String _tmpTituloPropiedad;
            if (_cursor.isNull(_cursorIndexOfTituloPropiedad)) {
              _tmpTituloPropiedad = null;
            } else {
              _tmpTituloPropiedad = _cursor.getString(_cursorIndexOfTituloPropiedad);
            }
            _item = new SolicitudEntity(_tmpId,_tmpUsuarioId,_tmpUsuarioEmail,_tmpCasaId,_tmpFecha,_tmpEstado,_tmpTituloPropiedad);
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

  @Override
  public Flow<List<SolicitudEntity>> obtenerTodas() {
    final String _sql = "SELECT * FROM solicitudes ORDER BY fecha DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"solicitudes"}, new Callable<List<SolicitudEntity>>() {
      @Override
      @NonNull
      public List<SolicitudEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfUsuarioId = CursorUtil.getColumnIndexOrThrow(_cursor, "usuarioId");
          final int _cursorIndexOfUsuarioEmail = CursorUtil.getColumnIndexOrThrow(_cursor, "usuarioEmail");
          final int _cursorIndexOfCasaId = CursorUtil.getColumnIndexOrThrow(_cursor, "casaId");
          final int _cursorIndexOfFecha = CursorUtil.getColumnIndexOrThrow(_cursor, "fecha");
          final int _cursorIndexOfEstado = CursorUtil.getColumnIndexOrThrow(_cursor, "estado");
          final int _cursorIndexOfTituloPropiedad = CursorUtil.getColumnIndexOrThrow(_cursor, "tituloPropiedad");
          final List<SolicitudEntity> _result = new ArrayList<SolicitudEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SolicitudEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final long _tmpUsuarioId;
            _tmpUsuarioId = _cursor.getLong(_cursorIndexOfUsuarioId);
            final String _tmpUsuarioEmail;
            _tmpUsuarioEmail = _cursor.getString(_cursorIndexOfUsuarioEmail);
            final int _tmpCasaId;
            _tmpCasaId = _cursor.getInt(_cursorIndexOfCasaId);
            final String _tmpFecha;
            _tmpFecha = _cursor.getString(_cursorIndexOfFecha);
            final String _tmpEstado;
            _tmpEstado = _cursor.getString(_cursorIndexOfEstado);
            final String _tmpTituloPropiedad;
            if (_cursor.isNull(_cursorIndexOfTituloPropiedad)) {
              _tmpTituloPropiedad = null;
            } else {
              _tmpTituloPropiedad = _cursor.getString(_cursorIndexOfTituloPropiedad);
            }
            _item = new SolicitudEntity(_tmpId,_tmpUsuarioId,_tmpUsuarioEmail,_tmpCasaId,_tmpFecha,_tmpEstado,_tmpTituloPropiedad);
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
