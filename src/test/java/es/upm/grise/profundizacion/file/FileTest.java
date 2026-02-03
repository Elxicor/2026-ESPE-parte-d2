package es.upm.grise.profundizacion.file;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import es.upm.grise.profundizacion.exceptions.InvalidContentException;
import es.upm.grise.profundizacion.exceptions.WrongFileTypeException;

public class FileTest {
    
    private File file;

    @BeforeEach
    public void setUp() {
        file = new File();
    }

    // 1. Pruebas para addProperty

    @Test
    public void testAddProperty_NullContent_ThrowsInvalidContentException() {
        // Especificación: "Si newcontent es null, se lanzará una InvalidContentException."
        assertThrows(InvalidContentException.class, () -> {
            file.addProperty(null);
        });
    }

    @Test
    public void testAddProperty_ImageFileType_ThrowsWrongFileTypeException() {
        // Especificación: "Si el type del file es IMAGE, se lanzará una excepción WrongFileTypeException."
        file.setType(FileType.IMAGE);
        char[] newContent = {'a', 'b', 'c'};
        
        assertThrows(WrongFileTypeException.class, () -> {
            file.addProperty(newContent);
        });
    }

    @Test
    public void testAddProperty_PropertyFileType_AddsContentCorrectly() throws Exception {
        // Especificación: "Este método añade un par clave=valor al content de un file"
        file.setType(FileType.PROPERTY);
        char[] newContent = {'D', 'A', 'T', 'E'};
        
        file.addProperty(newContent);
        
        assertEquals(4, file.getContent().size(), "El tamaño del contenido debería ser 4");
        assertEquals('D', file.getContent().get(0));
        assertEquals('E', file.getContent().get(3));
    }

    @Test
    public void testAddProperty_AppendsToExistingContent() throws Exception {
        // Especificación: "newcontent se añade al content existente."
        file.setType(FileType.PROPERTY);
        
        file.addProperty(new char[] {'A'});
        file.addProperty(new char[] {'B'});
        
        assertEquals(2, file.getContent().size());
        assertEquals('A', file.getContent().get(0));
        assertEquals('B', file.getContent().get(1));
    }

    // 2. Pruebas para getCRC32

    @Test
    public void testGetCRC32_EmptyContent_ReturnsZero() {
        // Especificación: "si content está vacío, getCRC32() devolverá el valor 0 (cero)."
        assertEquals(0L, file.getCRC32(), "El CRC32 de un archivo vacío debe ser 0");
    }

    @Test
    public void testGetCRC32_WithContent_CalculatesCRCUsingFileUtils() throws Exception {
        // Especificación: "Este método calcula el CRC32... devuelve el valor CRC32... mediante la clase FileUtils."
        
        // Configuración
        file.setType(FileType.PROPERTY);
        file.addProperty(new char[]{'T'}); // Añadimos contenido
        
        long expectedCrc = 12345L;

        // Dado que File.java hace 'new FileUtils()' internamente, usamos MockedConstruction para interceptarlo
        // Nota: Esto requiere mockito-inline o una versión reciente de Mockito.
        try (MockedConstruction<FileUtils> mockedFileUtils = mockConstruction(FileUtils.class,
                (mock, context) -> {
                    // Cuando se llame a calculateCRC32 con cualquier byte[], devolver el valor esperado
                    when(mock.calculateCRC32(any(byte[].class))).thenReturn(expectedCrc);
                })) {

            // Ejecución
            long result = file.getCRC32();

            // Verificación
            assertEquals(expectedCrc, result, "Debe devolver el CRC calculado por FileUtils");
            
            // Verificamos que se instanció FileUtils una vez
            assertEquals(1, mockedFileUtils.constructed().size());
            
            // Verificamos que se llamó al método calculateCRC32
            verify(mockedFileUtils.constructed().get(0)).calculateCRC32(any(byte[].class));
        }
        // Nota: Es posible que este test falle con IndexOutOfBoundsException debido a un bug 
        // en el código fuente proporcionado (byte[] bytes = new byte[content.size()] vs uso de i*2),
        // pero esta prueba verifica correctamente la especificación y la interacción esperada.
    }
}