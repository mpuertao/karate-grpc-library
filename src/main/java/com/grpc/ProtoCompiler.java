package com.grpc;

import java.io.File;
import java.io.IOException;

public class ProtoCompiler {
    /**
     * Genera un archivo descriptor (.desc) a partir de un .proto usando protoc.
     * @param protoPath Ruta al archivo .proto
     * @param descPath Ruta de salida del archivo .desc
     * @throws IOException Si hay un error de ejecución
     * @throws InterruptedException Si se interrumpe el proceso
     */
    public static void generateDescriptor(String protoPath, String descPath) throws IOException, InterruptedException {
        // Asegúrate de que 'protoc' esté instalado y disponible en el PATH
        ProcessBuilder pb = new ProcessBuilder(
                "protoc",
                "--proto_path=" + new File(protoPath).getParent(),
                "--descriptor_set_out=" + descPath,
                new File(protoPath).getName()
        );
        pb.directory(new File(protoPath).getParentFile());
        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("protoc failed with exit code " + exitCode );
        }
    }
}
