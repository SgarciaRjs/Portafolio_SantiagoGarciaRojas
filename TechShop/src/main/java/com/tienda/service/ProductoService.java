package com.tienda.service;

import com.tienda.domain.Producto;
import com.tienda.repository.ProductoRepository;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final FirebaseStorageService firebaseStorageService;

    public ProductoService(ProductoRepository productoRepository,
                           FirebaseStorageService firebaseStorageService) {
        this.productoRepository = productoRepository;
        this.firebaseStorageService = firebaseStorageService;
    }

    @Transactional(readOnly = true)
    public List<Producto> getProductos(boolean activo) {
        if (activo) {
            return productoRepository.findByActivoTrue();
        }
        return productoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Producto> getProducto(Integer idProducto) {
        return productoRepository.findById(idProducto);
    }

    @Transactional
    public void save(Producto producto, MultipartFile imagenFile) {
        producto = productoRepository.save(producto);
        if (imagenFile != null && !imagenFile.isEmpty() && firebaseStorageService.isAvailable()) {
            try {
                String urlImagen = firebaseStorageService.uploadImage(
                        imagenFile,
                        "productos",
                        producto.getIdProducto()
                );
                producto.setRutaImagen(urlImagen);
                productoRepository.save(producto);
            } catch (IOException e) {
                throw new IllegalStateException("Error al subir la imagen del producto", e);
            }
        }
    }

    @Transactional
    public void delete(Integer idProducto) {
        if (idProducto == null) {
            throw new IllegalArgumentException("El idProducto no puede ser nulo");
        }

        Optional<Producto> productoOpt = productoRepository.findById(idProducto);

        if (productoOpt.isEmpty()) {
            throw new IllegalArgumentException("El producto no existe");
        }

        try {
            productoRepository.deleteById(idProducto);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("No se puede eliminar el producto porque tiene datos asociados", e);
        }
    }
}