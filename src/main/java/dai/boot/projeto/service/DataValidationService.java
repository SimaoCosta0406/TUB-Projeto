package dai.boot.projeto.service;

import dai.boot.projeto.entities.Route;
import dai.boot.projeto.entities.Stop;
import dai.boot.projeto.entities.Vehicle;
import dai.boot.projeto.repository.RouteRepository;
import dai.boot.projeto.repository.StopRepository;
import dai.boot.projeto.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 5.3 - Validar Integração de Verticais
 * Valida a integridade e consistência dos dados entre diferentes verticais (rotas, veículos, paragens)
 */
@Service
public class DataValidationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataValidationService.class);

    @Autowired
    private RouteRepository routeRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private StopRepository stopRepository;

    /**
     * Validação completa da integração de verticais
     */
    public Map<String, Object> validateIntegration() {
        logger.info("[5.3] Iniciando validação de integração de verticais...");
        
        Map<String, Object> validationResult = new HashMap<>();
        
        // 1. Validar Rotas
        List<Map<String, Object>> routeValidation = validateRoutes();
        validationResult.put("routes", routeValidation);
        
        // 2. Validar Veículos
        List<Map<String, Object>> vehicleValidation = validateVehicles();
        validationResult.put("vehicles", vehicleValidation);
        
        // 3. Validar Paragens
        List<Map<String, Object>> stopValidation = validateStops();
        validationResult.put("stops", stopValidation);
        
        // 4. Validar Relacionamentos
        List<Map<String, Object>> relationshipValidation = validateRelationships();
        validationResult.put("relationships", relationshipValidation);
        
        // 5. Score geral
        int overallScore = calculateValidationScore(routeValidation, vehicleValidation, 
                                                     stopValidation, relationshipValidation);
        validationResult.put("overallValidationScore", overallScore);
        validationResult.put("validationStatus", overallScore >= 80 ? "PASS" : 
                                                  overallScore >= 60 ? "WARNING" : "FAIL");
        validationResult.put("validatedAt", LocalDateTime.now());
        
        logger.info("[5.3] Validação concluída com score: {}", overallScore);
        
        return validationResult;
    }

    /**
     * Validar integridade das rotas
     */
    private List<Map<String, Object>> validateRoutes() {
        List<Map<String, Object>> issues = new ArrayList<>();
        
        for (Route route : routeRepository.findAll()) {
            Map<String, Object> routeCheck = new HashMap<>();
            routeCheck.put("routeId", route.getId());
            routeCheck.put("routeCode", route.getCode());
            routeCheck.put("issues", new ArrayList<String>());
            
            List<String> routeIssues = (List<String>) routeCheck.get("issues");
            
            // Verificar se código é obrigatório
            if (route.getCode() == null || route.getCode().isEmpty()) {
                routeIssues.add("Código da rota está vazio");
            }
            
            // Verificar se origem/destino existem
            if (route.getOrigin() == null || route.getOrigin().isEmpty()) {
                routeIssues.add("Origem não definida");
            }
            if (route.getDestination() == null || route.getDestination().isEmpty()) {
                routeIssues.add("Destino não definido");
            }
            
            // Verificar se tem pelo menos 2 paragens
            if (route.getStops() == null || route.getStops().size() < 2) {
                routeIssues.add("Rota deve ter pelo menos 2 paragens");
            }
            
            // Verificar status válido
            String validStatuses = "ACTIVE|INACTIVE|MAINTENANCE";
            if (route.getStatus() == null || !validStatuses.contains(route.getStatus())) {
                routeIssues.add("Status inválido: " + route.getStatus());
            }
            
            routeCheck.put("isValid", routeIssues.isEmpty());
            if (!routeIssues.isEmpty()) {
                issues.add(routeCheck);
            }
        }
        
        logger.info("[5.3.1] Validação de rotas: {} problemas encontrados", issues.size());
        return issues;
    }

    /**
     * Validar integridade dos veículos
     */
    private List<Map<String, Object>> validateVehicles() {
        List<Map<String, Object>> issues = new ArrayList<>();
        
        for (Vehicle vehicle : vehicleRepository.findAll()) {
            Map<String, Object> vehicleCheck = new HashMap<>();
            vehicleCheck.put("vehicleId", vehicle.getId());
            vehicleCheck.put("vehiclePlate", vehicle.getPlate());
            vehicleCheck.put("issues", new ArrayList<String>());
            
            List<String> vehicleIssues = (List<String>) vehicleCheck.get("issues");
            
            // Verificar se matrícula é obrigatória
            if (vehicle.getPlate() == null || vehicle.getPlate().isEmpty()) {
                vehicleIssues.add("Matrícula está vazia");
            }
            
            // Verificar se modelo existe
            if (vehicle.getModel() == null || vehicle.getModel().isEmpty()) {
                vehicleIssues.add("Modelo não definido");
            }
            
            // Verificar se capacidade é válida
            if (vehicle.getCapacity() == null || vehicle.getCapacity() <= 0) {
                vehicleIssues.add("Capacidade inválida ou não definida");
            }
            
            // Verificar status válido
            String validStatuses = "IN_SERVICE|OUT_OF_SERVICE|MAINTENANCE";
            if (vehicle.getStatus() == null || !validStatuses.contains(vehicle.getStatus())) {
                vehicleIssues.add("Status inválido: " + vehicle.getStatus());
            }
            
            // Verificar se tem rota associada
            if ("IN_SERVICE".equals(vehicle.getStatus()) && vehicle.getRoute() == null) {
                vehicleIssues.add("Veículo em serviço sem rota associada");
            }
            
            vehicleCheck.put("isValid", vehicleIssues.isEmpty());
            if (!vehicleIssues.isEmpty()) {
                issues.add(vehicleCheck);
            }
        }
        
        logger.info("[5.3.2] Validação de veículos: {} problemas encontrados", issues.size());
        return issues;
    }

    /**
     * Validar integridade das paragens
     */
    private List<Map<String, Object>> validateStops() {
        List<Map<String, Object>> issues = new ArrayList<>();
        
        for (Stop stop : stopRepository.findAll()) {
            Map<String, Object> stopCheck = new HashMap<>();
            stopCheck.put("stopId", stop.getId());
            stopCheck.put("stopName", stop.getName());
            stopCheck.put("issues", new ArrayList<String>());
            
            List<String> stopIssues = (List<String>) stopCheck.get("issues");
            
            // Verificar se nome é obrigatório
            if (stop.getName() == null || stop.getName().isEmpty()) {
                stopIssues.add("Nome da paragem está vazio");
            }
            
            // Verificar se tem coordenadas
            if (stop.getLatitude() == null || stop.getLongitude() == null) {
                stopIssues.add("Coordenadas GPS não definidas");
            }
            
            // Verificar se coordenadas são válidas
            if (stop.getLatitude() != null && (stop.getLatitude() < -90 || stop.getLatitude() > 90)) {
                stopIssues.add("Latitude fora do intervalo válido");
            }
            if (stop.getLongitude() != null && (stop.getLongitude() < -180 || stop.getLongitude() > 180)) {
                stopIssues.add("Longitude fora do intervalo válido");
            }
            
            stopCheck.put("isValid", stopIssues.isEmpty());
            if (!stopIssues.isEmpty()) {
                issues.add(stopCheck);
            }
        }
        
        logger.info("[5.3.3] Validação de paragens: {} problemas encontrados", issues.size());
        return issues;
    }

    /**
     * Validar relacionamentos entre verticais
     */
    private List<Map<String, Object>> validateRelationships() {
        List<Map<String, Object>> issues = new ArrayList<>();
        
        List<Route> allRoutes = routeRepository.findAll();
        List<Vehicle> allVehicles = vehicleRepository.findAll();
        List<Stop> allStops = stopRepository.findAll();
        
        // 1. Rotas sem veículos
        for (Route route : allRoutes) {
            if ("ACTIVE".equals(route.getStatus()) && 
                (route.getVehicles() == null || route.getVehicles().isEmpty())) {
                
                Map<String, Object> relationshipIssue = new HashMap<>();
                relationshipIssue.put("type", "ROUTE_WITHOUT_VEHICLES");
                relationshipIssue.put("routeId", route.getId());
                relationshipIssue.put("routeCode", route.getCode());
                relationshipIssue.put("issue", "Rota ativa sem veículos associados");
                issues.add(relationshipIssue);
            }
        }
        
        // 2. Veículos em serviço sem rotas
        for (Vehicle vehicle : allVehicles) {
            if ("IN_SERVICE".equals(vehicle.getStatus()) && vehicle.getRoute() == null) {
                Map<String, Object> relationshipIssue = new HashMap<>();
                relationshipIssue.put("type", "VEHICLE_WITHOUT_ROUTE");
                relationshipIssue.put("vehicleId", vehicle.getId());
                relationshipIssue.put("vehiclePlate", vehicle.getPlate());
                relationshipIssue.put("issue", "Veículo em serviço sem rota");
                issues.add(relationshipIssue);
            }
        }
        
        // 3. Rotas com paragens inexistentes
        for (Route route : allRoutes) {
            if (route.getStops() != null) {
                for (String stopRef : route.getStops()) {
                    boolean stopExists = allStops.stream()
                        .anyMatch(s -> s.getName().equalsIgnoreCase(stopRef) || 
                                      (s.getId() != null && s.getId().toString().equals(stopRef)));
                    
                    if (!stopExists) {
                        Map<String, Object> relationshipIssue = new HashMap<>();
                        relationshipIssue.put("type", "STOP_NOT_FOUND");
                        relationshipIssue.put("routeId", route.getId());
                        relationshipIssue.put("routeCode", route.getCode());
                        relationshipIssue.put("stopReference", stopRef);
                        relationshipIssue.put("issue", "Paragem não encontrada: " + stopRef);
                        issues.add(relationshipIssue);
                    }
                }
            }
        }
        
        logger.info("[5.3.4] Validação de relacionamentos: {} problemas encontrados", issues.size());
        return issues;
    }

    /**
     * Calcular score geral de validação
     */
    private int calculateValidationScore(List<Map<String, Object>> routeIssues,
                                         List<Map<String, Object>> vehicleIssues,
                                         List<Map<String, Object>> stopIssues,
                                         List<Map<String, Object>> relationshipIssues) {
        
        long totalItems = routeRepository.count() + 
                         vehicleRepository.count() + 
                         stopRepository.count();
        
        int totalIssues = routeIssues.size() + vehicleIssues.size() + 
                         stopIssues.size() + relationshipIssues.size();
        
        if (totalItems == 0) return 100;
        
        int score = (int)(100 * (1.0 - (double) totalIssues / totalItems));
        return Math.max(0, score);
    }
}
