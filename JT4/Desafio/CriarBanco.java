package com.meuprojeto;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CriarBanco {
    public static void main(String[] args) {
        Connection conn = ConexaoSQLite.conectar();
        if (conn == null) {
            System.out.println("Erro ao conectar ao banco!");
            return;
        }

        String sql = "CREATE TABLE IF NOT EXISTS pessoa ("
                   + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                   + "nome TEXT NOT NULL, "
                   + "idade INTEGER NOT NULL, "
                   + "salario REAL NOT NULL)";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabela 'pessoa' criada com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabela: " + e.getMessage());
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
}