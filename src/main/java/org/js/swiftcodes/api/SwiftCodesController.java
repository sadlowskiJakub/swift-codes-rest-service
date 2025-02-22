package org.js.swiftcodes.api;

import lombok.extern.apachecommons.CommonsLog;
import org.js.swiftcodes.api.model.BankData;
import org.js.swiftcodes.api.model.Error;
import org.js.swiftcodes.service.AddOneSwiftCodeService;
import org.js.swiftcodes.service.DeleteSwiftCodeService;
import org.js.swiftcodes.service.GetBanksDataByCountryCodeService;
import org.js.swiftcodes.service.SingleSwiftCodeGetService;
import org.js.swiftcodes.service.exceptions.CountryISO2CodeNotFoundException;
import org.js.swiftcodes.service.exceptions.SwiftCodeAlreadyExistException;
import org.js.swiftcodes.service.exceptions.SwiftCodeInvalidException;
import org.js.swiftcodes.service.exceptions.SwiftCodeNotFoundException;
import org.js.swiftcodes.service.util.SwiftCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/")
@CommonsLog
public class SwiftCodesController {
    private final SingleSwiftCodeGetService singleSwiftCodeGetService;
    private final DeleteSwiftCodeService deleteSwiftCodeService;
    private final GetBanksDataByCountryCodeService getBanksDataByCountryCodeService;
    private final AddOneSwiftCodeService addOneSwiftCodeService;

    @Autowired
    public SwiftCodesController(SingleSwiftCodeGetService singleSwiftCodeGetService,
        DeleteSwiftCodeService deleteSwiftCodeService,
        GetBanksDataByCountryCodeService getBanksDataByCountryCodeService,
        AddOneSwiftCodeService addOneSwiftCodeService) {
        this.singleSwiftCodeGetService = singleSwiftCodeGetService;
        this.deleteSwiftCodeService = deleteSwiftCodeService;
        this.getBanksDataByCountryCodeService = getBanksDataByCountryCodeService;
        this.addOneSwiftCodeService = addOneSwiftCodeService;
    }

    @GetMapping("swift-codes/{swift-code}")
    public ResponseEntity<BankData> getSwiftCode(@PathVariable("swift-code") String swiftCode) {
        SwiftCodeUtil.validateSwiftCode(swiftCode);
        return ResponseEntity.ok(singleSwiftCodeGetService.getSwiftCode(swiftCode));
    }

    @GetMapping("swift-codes/country/{countryISO2code}")
    public ResponseEntity<List<BankData>> getAllSwiftCodesForSpecificCountry(@PathVariable("countryISO2code") String countryISO2Code) {
        return ResponseEntity.ok(getBanksDataByCountryCodeService.getBanksData(countryISO2Code));
    }

    @DeleteMapping("swift-codes/{swift-code}")
    public ResponseEntity<Map<String, String>> deleteSwiftCode(@PathVariable("swift-code") String swiftCode, @RequestParam String bankName, @RequestParam String countryISO2Code) {
        int deletedCount = deleteSwiftCodeService.deleteSwiftCode(swiftCode, bankName, countryISO2Code);
        return ResponseEntity.ok()
            .body(Map.of("message", String.format("Successfully deleted %d record with SWIFT code: %s", deletedCount, swiftCode)));
    }

    @PostMapping("swift-codes/")
    public ResponseEntity<Map<String, String>> addSwiftCode(@RequestBody BankData bankData) {
        int insertedCount = addOneSwiftCodeService.addSwiftCode(bankData.getAddress()
                .toUpperCase(),
            bankData.getBankName()
                .toUpperCase(),
            bankData.getCountryISO2()
                .toUpperCase(),
            bankData.getCountryName()
                .toUpperCase(),
            bankData.isHeadquarter(),
            bankData.getSwiftCode()
                .toUpperCase());
        return ResponseEntity.ok()
            .body(Map.of("message",
                String.format("Successfully added %d record with SWIFT code: %s",
                    insertedCount,
                    bankData.getSwiftCode()
                        .toUpperCase())));
    }

    @ExceptionHandler(SwiftCodeNotFoundException.class)
    public ResponseEntity<Error> handleSwiftCodeNotFoundException(SwiftCodeNotFoundException ex, WebRequest request) {
        Error response = Error.of("NOT_FOUND", ex.getMessage());
        log.warn("Handled SwiftCodeNotFoundException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    @ExceptionHandler(SwiftCodeInvalidException.class)
    public ResponseEntity<Error> handleInvalidSwiftCodeException(Exception ex, WebRequest request) {
        Error response = Error.of("INVALID_INPUT", ex.getMessage());
        log.warn("Handled SwiftCodeInvalidException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    @ExceptionHandler(SwiftCodeAlreadyExistException.class)
    public ResponseEntity<Error> handleSwiftCodeAlreadyExistException(Exception ex, WebRequest request) {
        Error response = Error.of("ALREADY_EXIST", ex.getMessage());
        log.warn("Handled SwiftCodeAlreadyExistException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }

    @ExceptionHandler(CountryISO2CodeNotFoundException.class)
    public ResponseEntity<Error> handleCountryISO2CodeNotFoundException(Exception ex, WebRequest request) {
        Error response = Error.of("NOT_FOUND", ex.getMessage());
        log.warn("Handled CountryISO2CodeNotFoundException: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .contentType(MediaType.APPLICATION_JSON)
            .body(response);
    }
}
