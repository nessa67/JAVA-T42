package com.meuprojeto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Connection conn = ConexaoSQLite.conectar();

        if (conn == null) {
            System.out.println("Erro ao conectar ao banco!");
            return;
        }

        int opcao;
        do {
            System.out.println("\n=== MENU PESSOA ===");
            System.out.println("1. Cadastrar Pessoa");
            System.out.println("2. Listar Pessoas");
            System.out.println("3. Atualizar Pessoa");
            System.out.println("4. Excluir Pessoa");
            System.out.println("5. Buscar Pessoa por ID");
            System.out.println("6. Sair");
            System.out.print("Escolha uma opção: ");
            opcao = scanner.nextInt();

            switch (opcao) {
                case 1:
                    cadastrarPessoa(conn, scanner);
                    break;
                case 2:
                    listarPessoas(conn);
                    break;
                case 3:
                    atualizarPessoa(conn, scanner);
                    break;
                case 4:
                    excluirPessoa(conn, scanner);
                    break;
                case 5:
                    buscarPessoaPorId(conn, scanner);
                    break;
                case 6:
                    System.out.println("Saindo do sistema...");
                    break;
                default:
                    System.out.println("Opção inválida!");
            }
        } while (opcao != 6);

        try {
            if (conn != null) conn.close();
            scanner.close();
        } catch (SQLException e) {
            System.err.println("Erro ao fechar conexão: " + e.getMessage());
        }
    }

    // CREATE
    public static void cadastrarPessoa(Connection conn, Scanner scanner) {
        System.out.println("\n--- CADASTRAR PESSOA ---");
        scanner.nextLine(); // Limpar buffer
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("Idade: ");
        int idade = scanner.nextInt();
        System.out.print("Salário: ");
        double salario = scanner.nextDouble();

        String sql = "INSERT INTO pessoa(nome, idade, salario) VALUES(?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nome);
            pstmt.setInt(2, idade);
            pstmt.setDouble(3, salario);
            pstmt.executeUpdate();
            System.out.println("Pessoa cadastrada com sucesso!");
        } catch (SQLException e) {
            System.err.println("Erro ao cadastrar: " + e.getMessage());
        }
    }

    // READ (All)
    public static void listarPessoas(Connection conn) {
        String sql = "SELECT * FROM pessoa ORDER BY id";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n--- LISTA DE PESSOAS ---");
            while (rs.next()) {
                System.out.println(
                    "ID: " + rs.getInt("id") +
                    " | Nome: " + rs.getString("nome") +
                    " | Idade: " + rs.getInt("idade") +
                    " | Salário: R$" + String.format("%.2f", rs.getDouble("salario"))
                );
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar: " + e.getMessage());
        }
    }

    // UPDATE
    public static void atualizarPessoa(Connection conn, Scanner scanner) {
        System.out.println("\n--- ATUALIZAR PESSOA ---");
        listarPessoas(conn);
        System.out.print("\nDigite o ID da pessoa: ");
        int id = scanner.nextInt();

        if (!pessoaExiste(conn, id)) {
            System.out.println("Pessoa não encontrada!");
            return;
        }

        scanner.nextLine(); // Limpar buffer
        System.out.print("Novo nome (deixe em branco para manter): ");
        String novoNome = scanner.nextLine();
        System.out.print("Nova idade (digite 0 para manter): ");
        int novaIdade = scanner.nextInt();
        System.out.print("Novo salário (digite 0 para manter): ");
        double novoSalario = scanner.nextDouble();

        StringBuilder sql = new StringBuilder("UPDATE pessoa SET ");
        boolean temAlteracao = false;

        if (!novoNome.isEmpty()) {
            sql.append("nome = ?");
            temAlteracao = true;
        }

        if (novaIdade > 0) {
            if (temAlteracao) sql.append(", ");
            sql.append("idade = ?");
            temAlteracao = true;
        }

        if (novoSalario > 0) {
            if (temAlteracao) sql.append(", ");
            sql.append("salario = ?");
            temAlteracao = true;
        }

        if (!temAlteracao) {
            System.out.println("Nenhuma alteração informada!");
            return;
        }

        sql.append(" WHERE id = ?");

        try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;

            if (!novoNome.isEmpty()) {
                pstmt.setString(paramIndex++, novoNome);
            }
            if (novaIdade > 0) {
                pstmt.setInt(paramIndex++, novaIdade);
            }
            if (novoSalario > 0) {
                pstmt.setDouble(paramIndex++, novoSalario);
            }

            pstmt.setInt(paramIndex, id);

            int linhasAfetadas = pstmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("Pessoa atualizada com sucesso!");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar: " + e.getMessage());
        }
    }

    // DELETE
    public static void excluirPessoa(Connection conn, Scanner scanner) {
        System.out.println("\n--- EXCLUIR PESSOA ---");
        listarPessoas(conn);
        System.out.print("\nDigite o ID da pessoa: ");
        int id = scanner.nextInt();

        if (!pessoaExiste(conn, id)) {
            System.out.println("Pessoa não encontrada!");
            return;
        }

        scanner.nextLine(); // Limpar buffer
        System.out.print("Tem certeza? (S/N): ");
        String confirmacao = scanner.nextLine().toUpperCase();

        if (!confirmacao.equals("S")) {
            System.out.println("Operação cancelada!");
            return;
        }

        String sql = "DELETE FROM pessoa WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int linhasAfetadas = pstmt.executeUpdate();
            if (linhasAfetadas > 0) {
                System.out.println("Pessoa excluída com sucesso!");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao excluir: " + e.getMessage());
        }
    }

    // READ (by ID)
    public static void buscarPessoaPorId(Connection conn, Scanner scanner) {
        System.out.println("\n--- BUSCAR PESSOA ---");
        System.out.print("Digite o ID: ");
        int id = scanner.nextInt();

        String sql = "SELECT * FROM pessoa WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println(
                    "\n--- DADOS DA PESSOA ---\n" +
                    "ID: " + rs.getInt("id") + "\n" +
                    "Nome: " + rs.getString("nome") + "\n" +
                    "Idade: " + rs.getInt("idade") + "\n" +
                    "Salário: R$" + String.format("%.2f", rs.getDouble("salario"))
                );
            } else {
                System.out.println("Pessoa não encontrada!");
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar: " + e.getMessage());
        }
    }

    // Método auxiliar
    private static boolean pessoaExiste(Connection conn, int id) {
        String sql = "SELECT 1 FROM pessoa WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Erro ao verificar: " + e.getMessage());
            return false;
        }
    }
}