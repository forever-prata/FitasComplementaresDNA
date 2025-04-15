package fitaDNA;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ProcessadorDNA implements Runnable {
    private static final Map<Character, Character> mapaComplementar = new HashMap<>();
    private static final Set<String> arquivosProcessados = ConcurrentHashMap.newKeySet();
    private static final Object lock = new Object();

    static {
        mapaComplementar.put('A', 'T');
        mapaComplementar.put('T', 'A');
        mapaComplementar.put('C', 'G');
        mapaComplementar.put('G', 'C');
    }

    @Override
    public void run() {
        String caminhoZip = "arquivosDNA.zip";

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(caminhoZip))) {
            ZipEntry entrada;
            while ((entrada = zis.getNextEntry()) != null) {
                if (!entrada.isDirectory() && entrada.getName().endsWith(".txt")) {
                    synchronized (lock) {
                        if (arquivosProcessados.contains(entrada.getName())) {
                            continue;
                        }
                        arquivosProcessados.add(entrada.getName());
                    }

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        baos.write(buffer, 0, len);
                    }

                    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                    processarArquivo(bais, entrada.getName());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processarArquivo(InputStream is, String nomeArquivo) {
        List<String> linhasSaida = new ArrayList<>();
        List<String> fitasInvalidas = new ArrayList<>();
        int totalFitas = 0;
        int fitasValidas = 0;
        int fitasInvalidasCount = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                totalFitas++;
                String fita = linha.trim().toUpperCase();
                if (fita.matches("[ATCG]+")) {
                    linhasSaida.add(gerarComplementar(fita));
                    fitasValidas++;
                } else {
                    String linhaInvalida = "****FITA INVALIDA - " + linha;
                    linhasSaida.add(linhaInvalida);
                    fitasInvalidas.add("Fita " + totalFitas + ": " + linha);
                    fitasInvalidasCount++;
                }
            }

            String pastaSaida = "saida_arquivosDNA";
            File diretorio = new File(pastaSaida);
            if (!diretorio.exists()) {
                diretorio.mkdirs();
            }

            String nomeSimples = new File(nomeArquivo).getName();
            String nomeSaida = pastaSaida + File.separator + "saida_" + nomeSimples;

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(nomeSaida))) {
                for (String linha1 : linhasSaida) {
                    bw.write(linha1);
                    bw.newLine();
                }
            }

            System.out.println(Thread.currentThread().getName() + " finalizou: " + nomeArquivo);
            System.out.println(" - Total de fitas: " + totalFitas);
            System.out.println(" - Fitas válidas: " + fitasValidas);
            System.out.println(" - Fitas inválidas: " + fitasInvalidasCount);

            if (!fitasInvalidas.isEmpty()) {
                System.out.println(" - Lista de fitas inválidas:");
                for (String invalida : fitasInvalidas) {
                    System.out.println("   - " + invalida);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String gerarComplementar(String fita) {
        StringBuilder complementar = new StringBuilder();
        for (char base : fita.toCharArray()) {
            complementar.append(mapaComplementar.get(base));
        }
        return complementar.toString();
    }
}