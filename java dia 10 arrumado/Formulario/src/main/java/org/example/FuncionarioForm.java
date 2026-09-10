package org.example;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DnDConstants;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

public class FuncionarioForm extends JFrame {

    private final List<String> listaOcorrencias = new ArrayList<>();

    // Cor de borda de erro usada na validacao visual
    private static final Color COR_ERRO = new Color(220, 80, 80);
    private static final Border BORDA_ERRO =
            BorderFactory.createLineBorder(COR_ERRO, 2, true);

    private static final Pattern PADRAO_EMAIL =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");

    // Referencias aos campos que participam de mascara/validacao
    private JFormattedTextField campoCpf;
    private JFormattedTextField campoCpfDoc;
    private JFormattedTextField campoCep;
    private JFormattedTextField campoTelefone;
    private JFormattedTextField campoCelular;
    private JTextField campoEmail;

    // Referencias da barra de status para atualizar feedback
    private JPanel barraStatus;
    private JLabel lblAberto;
    private JLabel lblAtivo;
    private JLabel lblFeedbackSalvar;

    public FuncionarioForm() {
        setTitle("Funcionários");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmarFechar();
            }
        });
        setSize(1050, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(criarBarraStatus(), BorderLayout.NORTH);

        JPanel conteudo = new JPanel(new BorderLayout(0, 8));
        conteudo.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        conteudo.add(criarPainelCpfNome(), BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Principal", criarAbaPrincipal());
        abas.addTab("Documentação", criarAbaDocumentacao());
        abas.addTab("Contrato", criarAbaContrato());
        abas.addTab("Operacional", criarAbaOperacional());

        conteudo.add(abas, BorderLayout.CENTER);
        add(conteudo, BorderLayout.CENTER);
    }

    private static class PainelFotoContainer extends JPanel {
        private BufferedImage imagem;

        public PainelFotoContainer() {
            setOpaque(false);
        }

        public void setImagem(BufferedImage img) {
            this.imagem = img;
            repaint();
        }

        public BufferedImage getImagem() {
            return this.imagem;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int arc = 12;

            g2.setColor(new Color(45, 48, 52));
            g2.fillRoundRect(0, 0, w, h, arc, arc);

            if (imagem != null) {
                g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, arc, arc));
                g2.drawImage(imagem, 0, 0, w, h, this);
            } else {
                int cx = w / 2;
                int cy = h / 2 - 12;
                int headRadius = Math.max(16, Math.min(w, h) / 7);

                g2.setColor(new Color(80, 85, 95));
                g2.fillOval(cx - headRadius, cy - headRadius, headRadius * 2, headRadius * 2);
                g2.fillArc(cx - (headRadius * 2), cy + headRadius - 4, headRadius * 4, headRadius * 3, 0, 180);

                g2.setColor(new Color(150, 155, 165));
                g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
                String texto = "Sem foto";
                FontMetrics fm = g2.getFontMetrics();
                int x = (w - fm.stringWidth(texto)) / 2;
                int y = cy + headRadius * 3 + 16;
                g2.drawString(texto, x, y);
            }
            g2.dispose();
        }
    }

    private JPanel criarBarraStatus() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(60, 64, 68)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        this.barraStatus = barra;

        JPanel esquerda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        esquerda.setOpaque(false);

        JLabel lblStatusTag = new JLabel("Status:");
        lblStatusTag.setForeground(new Color(160, 165, 175));
        esquerda.add(lblStatusTag);

        lblAberto = new JLabel("Aberto");
        lblAberto.setForeground(new Color(80, 200, 120));
        lblAberto.setFont(lblAberto.getFont().deriveFont(Font.BOLD));
        esquerda.add(lblAberto);

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.putClientProperty("JButton.buttonType", "accent");
        btnSalvar.setMnemonic('S');
        btnSalvar.setToolTipText("Validar e salvar (Alt+S / Enter)");

        JButton btnConcluir = new JButton("Concluir");
        btnConcluir.setMnemonic('C');
        JButton btnExcluir = new JButton("Excluir");
        btnExcluir.setMnemonic('E');
        JButton btnOcorrencia = new JButton("Ocorrência");
        btnOcorrencia.setMnemonic('O');

        esquerda.add(btnSalvar);
        esquerda.add(btnConcluir);
        esquerda.add(btnExcluir);
        esquerda.add(btnOcorrencia);

        JLabel lblSituacaoTag = new JLabel("Situação:");
        lblSituacaoTag.setForeground(new Color(160, 165, 175));
        esquerda.add(lblSituacaoTag);

        lblAtivo = new JLabel("Ativo");
        lblAtivo.setForeground(new Color(80, 200, 120));
        lblAtivo.setFont(lblAtivo.getFont().deriveFont(Font.BOLD));
        esquerda.add(lblAtivo);

        JPanel direita = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        direita.setOpaque(false);

        lblFeedbackSalvar = new JLabel(" ");
        lblFeedbackSalvar.setForeground(new Color(160, 165, 175));
        lblFeedbackSalvar.setFont(lblFeedbackSalvar.getFont().deriveFont(Font.PLAIN, 11f));
        direita.add(lblFeedbackSalvar);

        JButton btnFechar = new JButton("Fechar");
        btnFechar.setMnemonic('F');
        direita.add(btnFechar);

        btnSalvar.addActionListener(e -> salvar());

        btnConcluir.addActionListener(e -> {
            // Só permite concluir se o cadastro estiver válido
            List<String> erros = validarFormulario();
            if (!erros.isEmpty()) {
                mostrarErrosValidacao(erros);
                return;
            }
            lblAberto.setText("Concluído");
            lblAberto.setForeground(new Color(100, 180, 255));
            JOptionPane.showMessageDialog(this, "Cadastro do funcionário concluído com sucesso!", "Concluir", JOptionPane.INFORMATION_MESSAGE);
        });

        btnExcluir.addActionListener(e -> {
            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja realmente excluir este funcionário?",
                    "Confirmar Exclusão",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (resposta == JOptionPane.YES_OPTION) {
                lblAtivo.setText("Inativo");
                lblAtivo.setForeground(new Color(255, 99, 71));
                // Reforça o estado com um leve destaque na barra
                barraStatus.setBackground(new Color(58, 44, 44));
                barraStatus.setOpaque(true);
                barraStatus.repaint();
                JOptionPane.showMessageDialog(this, "Funcionário excluído/inativado com sucesso.", "Excluir", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnOcorrencia.addActionListener(e -> abrirJanelaOcorrencias());

        btnFechar.addActionListener(e -> confirmarFechar());

        // Enter aciona Salvar em qualquer lugar do formulário
        getRootPane().setDefaultButton(btnSalvar);

        barra.add(esquerda, BorderLayout.WEST);
        barra.add(direita, BorderLayout.EAST);

        return barra;
    }

    /** Valida o formulário; se válido, mostra feedback de sucesso com horário. */
    private void salvar() {
        List<String> erros = validarFormulario();
        if (!erros.isEmpty()) {
            mostrarErrosValidacao(erros);
            lblFeedbackSalvar.setText("Não salvo — verifique os campos");
            lblFeedbackSalvar.setForeground(COR_ERRO);
            return;
        }
        String hora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        lblFeedbackSalvar.setText("✓ Salvo às " + hora);
        lblFeedbackSalvar.setForeground(new Color(80, 200, 120));
        JOptionPane.showMessageDialog(this, "Dados salvos com sucesso!", "Salvar", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Exibe um resumo dos erros de validação em uma caixa de diálogo. */
    private void mostrarErrosValidacao(List<String> erros) {
        StringBuilder sb = new StringBuilder("Corrija os seguintes campos antes de continuar:\n");
        for (String erro : erros) {
            sb.append("  • ").append(erro).append('\n');
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Validação", JOptionPane.WARNING_MESSAGE);
    }

    /** Pergunta antes de fechar, evitando perda acidental de dados. */
    private void confirmarFechar() {
        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Deseja realmente fechar? Alterações não salvas serão perdidas.",
                "Confirmar Saída",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (resposta == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private void abrirJanelaOcorrencias() {
        JDialog dialog = new JDialog(this, "Ocorrências do Funcionário", true);
        dialog.setSize(520, 380);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (String oc : listaOcorrencias) {
            listModel.addElement(oc);
        }
        JList<String> listOcorrencias = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(listOcorrencias);

        JPanel painelEntrada = new JPanel(new BorderLayout(6, 0));
        JTextField txtNovaOcorrencia = new JTextField();
        JButton btnAdicionar = new JButton("Adicionar");
        btnAdicionar.putClientProperty("JButton.buttonType", "accent");

        painelEntrada.add(txtNovaOcorrencia, BorderLayout.CENTER);
        painelEntrada.add(btnAdicionar, BorderLayout.EAST);

        btnAdicionar.addActionListener(e -> {
            String texto = txtNovaOcorrencia.getText().trim();
            if (!texto.isEmpty()) {
                String dataHora = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                String registro = "[" + dataHora + "] " + texto;

                listaOcorrencias.add(registro);
                listModel.addElement(registro);
                txtNovaOcorrencia.setText("");
            }
        });

        JPanel painelPrincipal = new JPanel(new BorderLayout(8, 8));
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        painelPrincipal.add(new JLabel("Histórico de Ocorrências:"), BorderLayout.NORTH);
        painelPrincipal.add(scrollPane, BorderLayout.CENTER);
        painelPrincipal.add(painelEntrada, BorderLayout.SOUTH);

        dialog.add(painelPrincipal);
        dialog.setVisible(true);
    }

    private JPanel criarPainelCpfNome() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        painel.add(new JLabel("CPF"), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.25;
        campoCpf = criarCampoCpf("99362643120");
        campoCpf.setToolTipText("CPF do funcionário (11 dígitos)");
        painel.add(campoCpf, c);

        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0;
        painel.add(new JLabel("Nome"), c);

        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 1.0;
        painel.add(new JTextField("KARITTA REJANY PEREIRA"), c);

        return painel;
    }

    private JPanel criarAbaPrincipal() {
        JPanel aba = new JPanel();
        aba.setLayout(new BoxLayout(aba, BoxLayout.Y_AXIS));

        aba.add(criarPainelDadosGerais());
        aba.add(Box.createVerticalStrut(8));
        aba.add(criarPainelEndereco());
        aba.add(Box.createVerticalStrut(8));
        aba.add(criarPainelDadosPessoais());
        aba.add(Box.createVerticalGlue());

        return aba;
    }

    private JPanel criarPainelDadosGerais() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("Dados Gerais"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        painel.add(rotuloComCampo("Empresa", new JComboBox<>(new String[]{"1000 - EMPRESA RH"})), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.5;
        painel.add(rotuloComCampo("Divisão RH", new JComboBox<>(new String[]{"1001 - ADMINSTRACAO"})), c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.2;
        painel.add(rotuloComCampo("Matrícula", new JTextField("2")), c);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.8;
        painel.add(rotuloComCampo("Funcionário", new JTextField("KARITTA REJANY PEREIRA")), c);

        return painel;
    }

    private JPanel criarPainelEndereco() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("Endereço"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.15;

        JPanel cepPanel = new JPanel(new BorderLayout(2, 0));
        cepPanel.add(new JLabel("CEP"), BorderLayout.NORTH);

        JPanel cepCampo = new JPanel(new BorderLayout(4, 0));
        campoCep = criarCampoComMascara("#####-###", "75900055");
        campoCep.setToolTipText("CEP no formato 00000-000");
        cepCampo.add(campoCep, BorderLayout.CENTER);
        JButton btnCep = new JButton("?");
        btnCep.setMargin(new Insets(2, 8, 2, 8));
        cepCampo.add(btnCep, BorderLayout.EAST);

        cepPanel.add(cepCampo, BorderLayout.CENTER);
        painel.add(cepPanel, c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.45;
        painel.add(rotuloComCampo("Endereço", new JTextField("RUA ANTONIO GOMES QD 51 LT 06")), c);

        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0.10;
        painel.add(rotuloComCampo("N°", new JTextField("S/N")), c);

        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 0.30;
        painel.add(rotuloComCampo("Bairro", new JTextField("PROLONGAMENTO JARDIM AMÉRICA")), c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.30;
        painel.add(rotuloComCampo("Município", new JComboBox<>(new String[]{"2298 - Rio Verde - GO"})), c);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.20;
        campoTelefone = criarCampoComMascara("(##)####-####", "6436135118");
        campoTelefone.setToolTipText("Telefone fixo com DDD");
        painel.add(rotuloComCampo("Telefone", campoTelefone), c);

        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 0.20;
        campoCelular = criarCampoComMascara("(##)#####-####", "64992861225");
        campoCelular.setToolTipText("Celular com DDD");
        painel.add(rotuloComCampo("Telefone Cel.", campoCelular), c);

        c.gridx = 3;
        c.gridy = 1;
        c.weightx = 0.30;
        campoEmail = new JTextField("karitta@gmail.com");
        campoEmail.setToolTipText("E-mail do funcionário");
        painel.add(rotuloComCampo("Email", campoEmail), c);

        return painel;
    }

    private JPanel criarPainelDadosPessoais() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("Dados Pessoais"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 6, 4, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.2;
        painel.add(rotuloComCampo("Sexo", new JComboBox<>(new String[]{"Feminino", "Masculino"})), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.2;
        painel.add(rotuloComCampo("Data Nascimento", new JTextField("26/10/1982")), c);

        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0.25;
        painel.add(rotuloComCampo("Estado Civil", new JComboBox<>(new String[]{"Solteiro", "Casado", "Divorciado", "Viúvo"})), c);

        c.gridx = 3;
        c.gridy = 0;
        c.gridheight = 2;
        c.weightx = 0.35;
        painel.add(criarPainelFiliacao(), c);

        c.gridheight = 1;

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.2;
        painel.add(rotuloComCampo("Naturalidade", new JTextField("SÃO LUIS DE MONTES BELOS")), c);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.2;
        c.gridwidth = 2;
        painel.add(rotuloComCampo("Nacionalidade", new JTextField("BRASILEIRA")), c);

        c.gridwidth = 1;

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.2;
        painel.add(rotuloComCampo("Grau de Instrução", new JComboBox<>(new String[]{"Superior completo", "Superior incompleto", "Médio completo"})), c);

        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 0.2;
        c.gridwidth = 2;
        painel.add(rotuloComCampo("Formação", new JTextField()), c);

        return painel;
    }

    private JPanel criarPainelFiliacao() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("Filiação"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.weightx = 1.0;

        c.gridy = 0;
        painel.add(rotuloComCampo("Pai", new JTextField("JOAO QUIRINO PEREIRA")), c);

        c.gridy = 1;
        painel.add(rotuloComCampo("Mãe", new JTextField("MARIA PEREIRA")), c);

        return painel;
    }

    private JPanel criarAbaDocumentacao() {
        JPanel aba = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.NORTH;

        JPanel colunaEsquerda = new JPanel();
        colunaEsquerda.setLayout(new BoxLayout(colunaEsquerda, BoxLayout.Y_AXIS));

        colunaEsquerda.add(criarPainelRG());
        colunaEsquerda.add(Box.createVerticalStrut(6));
        colunaEsquerda.add(criarPainelInformacaoMilitar());
        colunaEsquerda.add(Box.createVerticalStrut(6));
        colunaEsquerda.add(criarPainelCNH());
        colunaEsquerda.add(Box.createVerticalStrut(6));
        colunaEsquerda.add(criarPainelConselhoRegional());

        JPanel colunaDireita = new JPanel();
        colunaDireita.setLayout(new BoxLayout(colunaDireita, BoxLayout.Y_AXIS));

        colunaDireita.add(criarPainelCTPS());
        colunaDireita.add(Box.createVerticalStrut(6));
        colunaDireita.add(criarPainelCpfDocumentacao());
        colunaDireita.add(Box.createVerticalStrut(6));
        colunaDireita.add(criarPainelPIS());
        colunaDireita.add(Box.createVerticalStrut(6));
        colunaDireita.add(criarPainelTituloEleitor());
        colunaDireita.add(Box.createVerticalStrut(6));
        colunaDireita.add(criarPainelRIC());

        c.gridx = 0;
        c.gridy = 0;
        aba.add(colunaEsquerda, c);

        c.gridx = 1;
        c.gridy = 0;
        aba.add(colunaDireita, c);

        return aba;
    }

    private JPanel criarPainelRG() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("RG"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.35;
        painel.add(rotuloComCampo("Número", new JTextField("42270594")), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.35;
        painel.add(rotuloComCampo("Órgão Expedidor", new JTextField("DGPC")), c);

        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0.15;
        painel.add(rotuloComCampo("UF", new JComboBox<>(new String[]{"GO"})), c);

        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 0.35;
        painel.add(rotuloComCampo("Data Expedição", new JComboBox<>(new String[]{"15/08/1996"})), c);

        return painel;
    }

    private JPanel criarPainelInformacaoMilitar() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("Informação Militar"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.6;
        painel.add(rotuloComCampo("Situação", new JTextField()), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.4;
        painel.add(rotuloComCampo("Data Baixa", new JComboBox<>(new String[]{""})), c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.6;
        painel.add(rotuloComCampo("Número", new JTextField()), c);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.4;
        painel.add(rotuloComCampo("Categoria", new JComboBox<>(new String[]{""})), c);

        return painel;
    }

    private JPanel criarPainelCNH() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("CNH"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.3;
        painel.add(rotuloComCampo("Número", new JTextField("04893241478")), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.2;
        painel.add(rotuloComCampo("Categoria", new JComboBox<>(new String[]{"AB"})), c);

        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0.25;
        painel.add(rotuloComCampo("Data Cadastro", new JComboBox<>(new String[]{"03/03/2010"})), c);

        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 0.25;
        painel.add(rotuloComCampo("Data Vencimento", new JComboBox<>(new String[]{"14/10/2014"})), c);

        return painel;
    }

    private JPanel criarPainelConselhoRegional() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("Conselho Regional"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        painel.add(rotuloComCampo("Nome", new JTextField()), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.2;
        painel.add(rotuloComCampo("Sigla", new JTextField()), c);

        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0.3;
        painel.add(rotuloComCampo("Reg. Região", new JTextField()), c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.25;
        painel.add(rotuloComCampo("Número", new JTextField()), c);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.25;
        painel.add(rotuloComCampo("Data Expedição", new JComboBox<>(new String[]{""})), c);

        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 0.25;
        painel.add(rotuloComCampo("Org. Emissor", new JComboBox<>(new String[]{""})), c);

        c.gridx = 3;
        c.gridy = 1;
        c.weightx = 0.25;
        painel.add(rotuloComCampo("Data Validade", new JComboBox<>(new String[]{""})), c);

        return painel;
    }

    private JPanel criarPainelCTPS() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("CTPS"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.35;
        painel.add(rotuloComCampo("Número", new JTextField("640194")), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.20;
        painel.add(rotuloComCampo("Série", new JTextField("030")), c);

        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0.20;
        painel.add(rotuloComCampo("Órgão", new JTextField("05")), c);

        c.gridx = 3;
        c.gridy = 0;
        c.weightx = 0.25;
        painel.add(rotuloComCampo("UF", new JComboBox<>(new String[]{"GO"})), c);

        return painel;
    }

    private JPanel criarPainelCpfDocumentacao() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("CPF"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        campoCpfDoc = criarCampoCpf("99362643120");
        campoCpfDoc.setToolTipText("CPF do funcionário (11 dígitos)");
        painel.add(rotuloComCampo("Número", campoCpfDoc), c);

        return painel;
    }

    private JPanel criarPainelPIS() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("PIS"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.6;
        painel.add(rotuloComCampo("Número", new JTextField("013434075325")), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.4;
        painel.add(rotuloComCampo("Data Cadastro", new JComboBox<>(new String[]{"01/02/2001"})), c);

        return painel;
    }

    private JPanel criarPainelTituloEleitor() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("Título de Eleitor"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.6;
        painel.add(rotuloComCampo("Número", new JTextField("46218231058")), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.2;
        painel.add(rotuloComCampo("Zona", new JTextField("140")), c);

        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0.2;
        painel.add(rotuloComCampo("Seção", new JTextField("123")), c);

        return painel;
    }

    private JPanel criarPainelRIC() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("RIC"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.4;
        painel.add(rotuloComCampo("Número", new JTextField()), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.3;
        painel.add(rotuloComCampo("Órgão Expedidor", new JTextField()), c);

        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0.3;
        painel.add(rotuloComCampo("Data Expedição", new JComboBox<>(new String[]{""})), c);

        return painel;
    }

    private JPanel criarAbaContrato() {
        JPanel aba = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.NORTH;

        JPanel colunaEsquerda = new JPanel();
        colunaEsquerda.setLayout(new BoxLayout(colunaEsquerda, BoxLayout.Y_AXIS));

        colunaEsquerda.add(rotuloComCampo("Vínculo", new JComboBox<>(new String[]{"15 - Trabalhador urbano vinculado a empregador pessoa física por con"})));
        colunaEsquerda.add(Box.createVerticalStrut(4));
        colunaEsquerda.add(rotuloComCampo("Tipo de Admissão", new JComboBox<>(new String[]{"Reemprego"})));
        colunaEsquerda.add(Box.createVerticalStrut(4));
        colunaEsquerda.add(criarLinhaAdmissao());
        colunaEsquerda.add(Box.createVerticalStrut(6));

        JPanel fgtsAtividade = new JPanel(new GridLayout(1, 2, 6, 0));
        fgtsAtividade.add(criarPainelFGTS());
        fgtsAtividade.add(criarPainelAtividadeDesenvolvida());

        colunaEsquerda.add(fgtsAtividade);
        colunaEsquerda.add(Box.createVerticalStrut(6));

        JPanel adiantamentoExperiencia = new JPanel(new GridLayout(1, 2, 6, 0));
        adiantamentoExperiencia.add(criarPainelAdiantamentoQuinzenal());
        adiantamentoExperiencia.add(criarPainelExperiencia());

        colunaEsquerda.add(adiantamentoExperiencia);
        colunaEsquerda.add(Box.createVerticalStrut(6));
        colunaEsquerda.add(criarLinhaValorSalario());

        JPanel colunaDireita = new JPanel();
        colunaDireita.setLayout(new BoxLayout(colunaDireita, BoxLayout.Y_AXIS));

        colunaDireita.add(rotuloComCampo("Cargo", new JComboBox<>(new String[]{"142105 - Gerente administrativo"})));
        colunaDireita.add(Box.createVerticalStrut(4));
        colunaDireita.add(rotuloComCampo("Departamento", new JComboBox<>(new String[]{"1 - ADMINISTRATIVO"})));
        colunaDireita.add(Box.createVerticalStrut(4));
        colunaDireita.add(rotuloComCampo("Categoria GFIP", new JComboBox<>(new String[]{"11 - Contribuinte individual - Diretor não empregado e demais e"})));
        colunaDireita.add(Box.createVerticalStrut(4));
        colunaDireita.add(rotuloComCampo("Tipo Contrato", new JComboBox<>(new String[]{""})));
        colunaDireita.add(Box.createVerticalStrut(6));
        colunaDireita.add(criarPainelRescisao());

        c.gridx = 0;
        c.gridy = 0;
        aba.add(colunaEsquerda, c);

        c.gridx = 1;
        c.gridy = 0;
        aba.add(colunaDireita, c);

        return aba;
    }

    private JPanel criarLinhaAdmissao() {
        JPanel painel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.weightx = 0.3;
        painel.add(rotuloComCampo("Data Admissão", new JComboBox<>(new String[]{"01/01/2010"})), c);

        c.gridx = 1;
        c.weightx = 0.3;
        painel.add(rotuloComCampo("Tipo de Salário", new JComboBox<>(new String[]{"Mensal"})), c);

        c.gridx = 2;
        c.weightx = 0.25;
        painel.add(rotuloComCampo("Horário", new JComboBox<>(new String[]{"GERAL"})), c);

        c.gridx = 3;
        c.weightx = 0.15;
        painel.add(rotuloComCampo("Hrs. Sem.", new JTextField("220")), c);

        return painel;
    }

    private JPanel criarPainelFGTS() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("FGTS"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        painel.add(rotuloComCampo("Opção", new JComboBox<>(new String[]{"Optante"})), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.5;
        painel.add(rotuloComCampo("Data Opção", new JComboBox<>(new String[]{"01/01/2010"})), c);

        return painel;
    }

    private JPanel criarPainelAtividadeDesenvolvida() {
        JPanel painel = new JPanel(new BorderLayout());
        painel.setBorder(criarBorda("Atividade Desenvolvida"));

        JPanel radios = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        JRadioButton urbana = new JRadioButton("Urbana", true);
        JRadioButton rural = new JRadioButton("Rural");

        ButtonGroup grupo = new ButtonGroup();
        grupo.add(urbana);
        grupo.add(rural);

        radios.add(urbana);
        radios.add(rural);

        painel.add(radios, BorderLayout.CENTER);

        return painel;
    }

    private JPanel criarPainelAdiantamentoQuinzenal() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("Adiantamento Quinzenal"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.4;
        painel.add(rotuloComCampo("Adiantamento", new JComboBox<>(new String[]{"Sim", "Não"})), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.3;
        painel.add(rotuloComCampo("Percentual", new JTextField("35,00")), c);

        c.gridx = 2;
        c.gridy = 0;
        c.weightx = 0.3;
        painel.add(rotuloComCampo("Valor Fixo", new JTextField("0,00")), c);

        return painel;
    }

    private JPanel criarPainelExperiencia() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("Experiência"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        painel.add(rotuloComCampo("Vencimento", new JComboBox<>(new String[]{"15/02/2010"})), c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1.0;
        painel.add(rotuloComCampo("Prorrogação", new JComboBox<>(new String[]{"01/04/2010"})), c);

        return painel;
    }

    private JPanel criarLinhaValorSalario() {
        JPanel painel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.weightx = 0.5;
        painel.add(rotuloComCampo("Valor Salário", new JTextField("2.620,00")), c);

        c.gridx = 1;
        c.weightx = 0.5;
        painel.add(rotuloComCampo("Tipo de Reajuste", new JComboBox<>(new String[]{"Variável"})), c);

        return painel;
    }

    private JPanel criarPainelRescisao() {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBorder(criarBorda("Rescisão"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 4, 3, 4);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.4;
        painel.add(rotuloComCampo("Data Demissão", new JComboBox<>(new String[]{""})), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.6;
        painel.add(rotuloComCampo("Motivo Demissão", new JComboBox<>(new String[]{""})), c);

        JCheckBox avisoPrevio = new JCheckBox("Aviso Prévio");

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.4;
        painel.add(avisoPrevio, c);

        JComboBox<String> dataAvisoInicio = new JComboBox<>(new String[]{""});
        dataAvisoInicio.setEnabled(false);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.3;
        painel.add(rotuloComCampo("Data Aviso Início", dataAvisoInicio), c);

        JComboBox<String> dataAvisoFim = new JComboBox<>(new String[]{""});
        dataAvisoFim.setEnabled(false);

        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 0.3;
        painel.add(rotuloComCampo("Data Aviso Fim", dataAvisoFim), c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 3;
        c.weightx = 1.0;

        painel.add(rotuloComCampo("Motivo RAIS", new JComboBox<>(new String[]{""})), c);

        c.gridwidth = 1;

        avisoPrevio.addActionListener(e -> {
            boolean ativo = avisoPrevio.isSelected();
            dataAvisoInicio.setEnabled(ativo);
            dataAvisoFim.setEnabled(ativo);
        });

        return painel;
    }

    private JPanel criarAbaOperacional() {
        JPanel aba = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 3, 3, 3);
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTH;

        JPanel beneficios = new JPanel(new GridLayout(0, 1, 2, 2));
        beneficios.add(new JCheckBox("INSS", true));
        beneficios.add(new JCheckBox("FGTS", true));
        beneficios.add(new JCheckBox("IRRF", true));
        beneficios.add(new JCheckBox("Reembolso INSS/IRRF"));
        beneficios.add(new JCheckBox("Empregado Doméstico"));
        beneficios.add(new JCheckBox("Benefício Previdência - Aposentadoria"));
        beneficios.add(new JCheckBox("Vale Transporte"));
        beneficios.add(new JCheckBox("Vale Refeição", true));
        beneficios.add(new JCheckBox("Plano de Saúde", true));

        JPanel sindicato = new JPanel(new GridBagLayout());
        sindicato.setBorder(criarBorda("Sindicato"));

        GridBagConstraints cs = new GridBagConstraints();
        cs.insets = new Insets(3, 4, 3, 4);
        cs.fill = GridBagConstraints.HORIZONTAL;

        cs.gridx = 0;
        cs.gridy = 0;
        cs.weightx = 0.75;
        sindicato.add(rotuloComCampo("Sindicato", new JComboBox<>(new String[]{"SINDICATO FUNCIONAL"})), cs);

        cs.gridx = 1;
        cs.gridy = 0;
        cs.weightx = 0.25;
        sindicato.add(rotuloComCampo("Mês Dissídio", new JComboBox<>(new String[]{"Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho", "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"})), cs);

        JPanel dadosFuncionario = new JPanel(new GridBagLayout());
        dadosFuncionario.setBorder(criarBorda("Dados Funcionário"));

        GridBagConstraints cd = new GridBagConstraints();
        cd.insets = new Insets(3, 4, 3, 4);
        cd.fill = GridBagConstraints.HORIZONTAL;

        cd.gridx = 0;
        cd.gridy = 0;
        cd.weightx = 0.35;
        dadosFuncionario.add(rotuloComCampo("Forma de Pagamento", new JComboBox<>(new String[]{"Débito em Conta"})), cd);

        cd.gridx = 1;
        cd.gridy = 0;
        cd.weightx = 0.25;
        dadosFuncionario.add(rotuloComCampo("Banco", new JComboBox<>(new String[]{"Banco do Brasil"})), cd);

        cd.gridx = 2;
        cd.gridy = 0;
        cd.weightx = 0.15;
        dadosFuncionario.add(rotuloComCampo("Agência", new JTextField("2665")), cd);

        cd.gridx = 3;
        cd.gridy = 0;
        cd.weightx = 0.25;
        dadosFuncionario.add(rotuloComCampo("Número Conta", new JTextField("44668")), cd);

        JPanel exames = new JPanel(new GridBagLayout());
        exames.setBorder(criarBorda("Exames Admissionais"));

        GridBagConstraints ce = new GridBagConstraints();
        ce.insets = new Insets(3, 4, 3, 4);
        ce.fill = GridBagConstraints.HORIZONTAL;

        ce.gridx = 0;
        ce.gridy = 0;
        ce.weightx = 0.5;
        exames.add(rotuloComCampo("Nome Médico", new JTextField()), ce);

        ce.gridx = 1;
        ce.gridy = 0;
        ce.weightx = 0.25;
        exames.add(rotuloComCampo("CRM", new JTextField()), ce);

        ce.gridx = 2;
        ce.gridy = 0;
        ce.weightx = 0.25;
        exames.add(rotuloComCampo("Data Exame", new JComboBox<>(new String[]{""})), ce);

        JPanel foto = new JPanel(new BorderLayout(6, 6));
        foto.setBorder(criarBorda("Foto"));

        PainelFotoContainer fotoContainer = new PainelFotoContainer();
        fotoContainer.setToolTipText("Clique em Selecionar ou arraste uma imagem aqui");
        foto.add(fotoContainer, BorderLayout.CENTER);

        // Permite arrastar e soltar um arquivo de imagem sobre o painel da foto
        new DropTarget(fotoContainer, DnDConstants.ACTION_COPY, new DropTargetAdapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void drop(DropTargetDropEvent evento) {
                try {
                    evento.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> arquivos = (List<File>) evento.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    if (arquivos != null && !arquivos.isEmpty()) {
                        BufferedImage img = ImageIO.read(arquivos.get(0));
                        if (img != null) {
                            fotoContainer.setImagem(img);
                        } else {
                            JOptionPane.showMessageDialog(FuncionarioForm.this,
                                    "O arquivo arrastado não é uma imagem válida.", "Erro",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                    evento.dropComplete(true);
                } catch (Exception ex) {
                    evento.dropComplete(false);
                }
            }
        });

        JPanel botoesFoto = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 4));
        JButton selecionar = new JButton("Selecionar");
        JButton limpar = new JButton("Limpar");

        botoesFoto.add(selecionar);
        botoesFoto.add(limpar);
        foto.add(botoesFoto, BorderLayout.SOUTH);

        selecionar.addActionListener(e -> {
            JFileChooser seletor = new JFileChooser();
            seletor.setDialogTitle("Selecionar foto do funcionário");
            seletor.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Imagens (*.jpg, *.jpeg, *.png, *.gif)", "jpg", "jpeg", "png", "gif"));

            if (seletor.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File arquivo = seletor.getSelectedFile();
                try {
                    BufferedImage imagemOriginal = ImageIO.read(arquivo);
                    if (imagemOriginal == null) {
                        JOptionPane.showMessageDialog(this, "O arquivo selecionado não é uma imagem válida.", "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    fotoContainer.setImagem(imagemOriginal);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Não foi possível carregar a imagem.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        limpar.addActionListener(e -> {
            if (fotoContainer.getImagem() == null) {
                return;
            }
            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja remover a foto do funcionário?",
                    "Limpar Foto",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (resposta == JOptionPane.YES_OPTION) {
                fotoContainer.setImagem(null);
            }
        });

        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 3;
        c.weightx = 0.25;
        c.weighty = 1.0;
        aba.add(beneficios, c);

        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 1;
        c.weightx = 0.45;
        c.weighty = 0;
        aba.add(sindicato, c);

        c.gridx = 2;
        c.gridy = 0;
        c.gridheight = 3;
        c.weightx = 0.30;
        c.weighty = 1.0;
        aba.add(foto, c);

        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 1;
        c.weightx = 0.45;
        c.weighty = 0;
        aba.add(dadosFuncionario, c);

        c.gridx = 1;
        c.gridy = 2;
        c.gridheight = 1;
        c.weightx = 0.45;
        c.weighty = 0;
        aba.add(exames, c);

        return aba;
    }

    // ===================== Máscaras e validação =====================

    /**
     * Cria um campo formatado com a máscara informada.
     * Ex.: "#####-###" para CEP, "(##)#####-####" para celular.
     */
    private JFormattedTextField criarCampoComMascara(String mascara, String valorInicial) {
        try {
            MaskFormatter formatador = new MaskFormatter(mascara);
            formatador.setPlaceholderCharacter('_');
            formatador.setValueContainsLiteralCharacters(false);
            JFormattedTextField campo = new JFormattedTextField(formatador);
            campo.setFocusLostBehavior(JFormattedTextField.COMMIT);
            if (valorInicial != null) {
                campo.setText(valorInicial);
            }
            return campo;
        } catch (java.text.ParseException e) {
            // Fallback seguro: se a máscara for inválida, devolve um campo simples
            JFormattedTextField campo = new JFormattedTextField();
            if (valorInicial != null) {
                campo.setText(valorInicial);
            }
            return campo;
        }
    }

    /** Campo de CPF com máscara 000.000.000-00. */
    private JFormattedTextField criarCampoCpf(String somenteDigitos) {
        return criarCampoComMascara("###.###.###-##", somenteDigitos);
    }

    /** Extrai apenas os dígitos de um texto (remove máscara/pontuação). */
    private static String somenteDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    /** Valida um CPF pelos dígitos verificadores. */
    private static boolean cpfValido(String cpf) {
        String d = somenteDigitos(cpf);
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        try {
            int[] num = new int[11];
            for (int i = 0; i < 11; i++) {
                num[i] = d.charAt(i) - '0';
            }
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += num[i] * (10 - i);
            }
            int dig1 = 11 - (soma % 11);
            if (dig1 >= 10) {
                dig1 = 0;
            }
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += num[i] * (11 - i);
            }
            int dig2 = 11 - (soma % 11);
            if (dig2 >= 10) {
                dig2 = 0;
            }
            return dig1 == num[9] && dig2 == num[10];
        } catch (Exception e) {
            return false;
        }
    }

    /** Marca um campo como inválido (borda vermelha) e adiciona o motivo à lista. */
    private void marcarInvalido(JComponent campo, String motivo, List<String> erros) {
        campo.setBorder(BORDA_ERRO);
        campo.putClientProperty("erroTooltipOriginal", campo.getToolTipText());
        campo.setToolTipText(motivo);
        erros.add(motivo);
    }

    /** Restaura o campo ao estado válido (borda padrão). */
    private void limparInvalido(JComponent campo) {
        campo.setBorder(UIManager.getBorder("TextField.border"));
        Object original = campo.getClientProperty("erroTooltipOriginal");
        if (original instanceof String) {
            campo.setToolTipText((String) original);
        }
    }

    /**
     * Executa a validação dos campos com regras (CPF e e-mail).
     * Retorna a lista de mensagens de erro (vazia = tudo válido).
     */
    private List<String> validarFormulario() {
        List<String> erros = new ArrayList<>();

        // CPF (cabeçalho)
        limparInvalido(campoCpf);
        if (!cpfValido(campoCpf.getText())) {
            marcarInvalido(campoCpf, "CPF inválido", erros);
        }

        // CPF (aba Documentação)
        limparInvalido(campoCpfDoc);
        if (!cpfValido(campoCpfDoc.getText())) {
            marcarInvalido(campoCpfDoc, "CPF (Documentação) inválido", erros);
        }

        // E-mail
        limparInvalido(campoEmail);
        String email = campoEmail.getText().trim();
        if (!email.isEmpty() && !PADRAO_EMAIL.matcher(email).matches()) {
            marcarInvalido(campoEmail, "E-mail inválido", erros);
        }

        // CEP obrigatório completo
        limparInvalido(campoCep);
        if (somenteDigitos(campoCep.getText()).length() != 8) {
            marcarInvalido(campoCep, "CEP incompleto", erros);
        }

        return erros;
    }

    private JPanel rotuloComCampo(String rotulo, JComponent campo) {
        if (campo instanceof JComboBox) {
            ((JComboBox<?>) campo).setEditable(true);
        }

        JPanel p = new JPanel(new BorderLayout(0, 3));
        JLabel lbl = new JLabel(rotulo);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 11f));
        lbl.setForeground(new Color(170, 175, 185));

        p.add(lbl, BorderLayout.NORTH);
        p.add(campo, BorderLayout.CENTER);

        return p;
    }

    private TitledBorder criarBorda(String titulo) {
        Border linhaCartao = BorderFactory.createLineBorder(new Color(60, 64, 68), 1, true);
        Border margemInterna = BorderFactory.createEmptyBorder(6, 8, 6, 8);
        Border bordaComposta = BorderFactory.createCompoundBorder(linhaCartao, margemInterna);

        TitledBorder borda = BorderFactory.createTitledBorder(bordaComposta, titulo);
        borda.setTitleFont(new Font("Segoe UI", Font.BOLD, 12));
        borda.setTitleColor(new Color(195, 200, 210));

        return borda;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                FlatDarkLaf.setup();
                UIManager.put("Component.arc", 8);
                UIManager.put("Button.arc", 8);
                UIManager.put("TextComponent.arc", 8);
            } catch (Exception e) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }
            }
            new FuncionarioForm().setVisible(true);
        });
    }
}