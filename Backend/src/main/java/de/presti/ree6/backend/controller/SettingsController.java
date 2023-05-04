package de.presti.ree6.backend.controller;

import de.presti.ree6.backend.repository.SettingRepository;
import de.presti.ree6.backend.service.SessionService;
import de.presti.ree6.sql.entities.Setting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping("/settings/{guildId}")
public class SettingsController {

    private final SessionService sessionService;
    private final SettingRepository settingRepository;

    @Autowired
    public SettingsController(SessionService sessionService, SettingRepository settingRepository) {
        this.sessionService = sessionService;
        this.settingRepository = settingRepository;
    }

    //region Settings Retrieve

    @CrossOrigin
    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SettingListResponse> retrieveSettings(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                      @PathVariable(name = "guildId") String guildId) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId).publishOn(Schedulers.boundedElastic()).flatMap(sessionContainer ->
                Mono.just(new SettingListResponse(true, settingRepository.getSettingsByGuildId(guildId), "Setting retrieved!")))
                .onErrorResume(e -> Mono.just(new SettingListResponse(false, null, e.getMessage())))
                .onErrorReturn(new SettingListResponse(false, null, "Server error!"));
    }

    @CrossOrigin
    @GetMapping(value = "/{settingName}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SettingResponse> retrieveSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                                 @PathVariable(name = "guildId") String guildId,
                                                 @PathVariable(name = "settingName") String settingName) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId).publishOn(Schedulers.boundedElastic()).flatMap(sessionContainer ->
                Mono.just(new SettingResponse(true, settingRepository.getSettingByGuildIdAndName(guildId, settingName), "Setting retrieved!")))
                .onErrorResume(e -> Mono.just(new SettingResponse(false, null, e.getMessage())))
                .onErrorReturn(new SettingResponse(false, null, "Server error!"));
    }

    //endregion

    //region Settings Update

    @CrossOrigin
    @GetMapping(value = "/{settingName}/update", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SettingResponse> updateSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                               @PathVariable(name = "guildId") String guildId,
                                               @PathVariable(name = "settingName") String settingName,
                                               @RequestBody String value) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId).publishOn(Schedulers.boundedElastic()).flatMap(sessionContainer -> {
                            Setting setting = settingRepository.getSettingByGuildIdAndName(guildId, settingName);
                            setting.setValue(value);
                            settingRepository.save(setting);
                            return Mono.just(new SettingResponse(true, setting, "Setting updated!"));
                        })
                .onErrorResume(e -> Mono.just(new SettingResponse(false, null, e.getMessage())))
                .onErrorReturn(new SettingResponse(false, null, "Server error!"));
    }

    //endregion

    //region Setting Delete

    @CrossOrigin
    @GetMapping(value = "/{settingName}/delete", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<SettingResponse> deleteSetting(@RequestHeader(name = "X-Session-Authenticator") String sessionIdentifier,
                                               @PathVariable(name = "guildId") String guildId,
                                               @PathVariable(name = "settingName") String settingName) {
        return sessionService.retrieveGuild(sessionIdentifier, guildId).publishOn(Schedulers.boundedElastic()).flatMap(sessionContainer -> {
                            Setting setting = settingRepository.getSettingByGuildIdAndName(guildId, settingName);
                            settingRepository.delete(setting);
                            return Mono.just(new SettingResponse(true, null, "Setting deleted!"));
                })
                .onErrorResume(e -> Mono.just(new SettingResponse(false, null, e.getMessage())))
                .onErrorReturn(new SettingResponse(false, null, "Server error!"));
    }

    //endregion


    public record SettingResponse(boolean success, Setting setting, String message) {
    }

    public record SettingListResponse(boolean success, List<Setting> settings, String message) {
    }
}
