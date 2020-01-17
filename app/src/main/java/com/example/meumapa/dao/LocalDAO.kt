package com.example.meumapa.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.meumapa.model.Local

class LocalDAO(context: Context?) : SQLiteOpenHelper(context, "meuMapa", null, 1) {


    override fun onCreate(db: SQLiteDatabase?) {
        val sql =
            "CREATE TABLE local_table(id INTEGER PRIMARY KEY AUTOINCREMENT, imagem TEXT, telefone TEXT, observacao TEXT, latlong TEXT)"
        db?.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val sql = "DROP TABLE IF EXISTS local_table"
        db?.execSQL(sql)
        onCreate(db)
    }

    fun insereLocal(local: Local) {
        val db: SQLiteDatabase = writableDatabase
        val dados: ContentValues = pegaLocal(local)
        db.insert("local", null, dados)
    }

    private fun pegaLocal(local: Local): ContentValues {
        val dados = ContentValues()
        dados.put("id", local.id)
        dados.put("imagem", local.imagem)
        dados.put("telefone", local.telefone)
        dados.put("observacao", local.observacao)
        dados.put("latlong", local.latLng)
        return dados
    }

    fun buscaLocal(): MutableList<Local> {
        val sql = "SELECT * FROM local_table"
        val db: SQLiteDatabase = readableDatabase
        val c: Cursor = db.rawQuery(sql, null)

        val locais: MutableList<Local> = arrayListOf()
        val local = Local()

        if (!c.equals(null)) {
            while (c.moveToNext()) {
                local.id = (c.getLong(c.getColumnIndex("id")))
                local.imagem = (c.getString(c.getColumnIndex("imagem")))
                local.telefone = (c.getString(c.getColumnIndex("telefone")))
                local.observacao = (c.getString(c.getColumnIndex("observacao")))
                local.latLng = (c.getString(c.getColumnIndex("latlong")))
                locais.add(local)
            }
        }
        c.close()
        return locais
    }

    fun deletaLocal(local: Local) {
        val db: SQLiteDatabase = writableDatabase
        val params: kotlin.Array<String> = arrayOf(local.id.toString())
        db.delete("local", "id = ?", params)
    }

    fun alteraLocal(local: Local) {
        val db: SQLiteDatabase = writableDatabase
        val values: ContentValues = pegaLocal(local = local)

        val params: kotlin.Array<String> = arrayOf(local.id.toString())
        db.update("local", values, "id=?", params)
    }
}