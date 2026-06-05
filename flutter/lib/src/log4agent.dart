import 'dart:convert';
import 'dart:math';

import 'package:dio/dio.dart';
import 'package:http/http.dart' as http;

import 'log4agent_level.dart';
import 'log4agent_config.dart';
import 'log4agent_redactor.dart';
import 'log4agent_transport.dart';

class Log4Agent {
  Log4Agent._();

  static Log4AgentConfig _config = const Log4AgentConfig();
  static Log4AgentTransport _transport = Log4AgentHttpTransport();
  static String _sessionId = _newSessionId();
  static bool _sessionStarted = false;
  static bool _initialized = false;

  static void configure({
    Log4AgentConfig config = const Log4AgentConfig(),
    http.Client? client,
    Dio? dio,
    Log4AgentTransport? transport,
    bool force = false,
  }) {
    if (_initialized && !force) return;
    _initialized = true;
    _config = config;
    _sessionId = _newSessionId();
    _sessionStarted = false;
    _transport.close();
    _transport =
        transport ??
        (dio != null
            ? Log4AgentDioTransport(dio)
            : Log4AgentHttpTransport(client));
    _startSession();
  }

  static void debug(
    String category,
    String message, {
    Map<String, String> attributes = const {},
  }) {
    log(category, message, level: Log4AgentLevel.debug, attributes: attributes);
  }

  static void info(
    String category,
    String message, {
    Map<String, String> attributes = const {},
  }) {
    log(category, message, level: Log4AgentLevel.info, attributes: attributes);
  }

  static void warn(
    String category,
    String message, {
    Map<String, String> attributes = const {},
  }) {
    log(category, message, level: Log4AgentLevel.warn, attributes: attributes);
  }

  static void error(
    String category,
    String message, {
    Map<String, String> attributes = const {},
  }) {
    log(category, message, level: Log4AgentLevel.error, attributes: attributes);
  }

  static void log(
    String category,
    String message, {
    Log4AgentLevel level = Log4AgentLevel.info,
    Map<String, String> attributes = const {},
  }) {
    final current = _config;
    if (!current.enabled || current.endpoint.isEmpty) return;
    _startSession();
    final safeMessage = current.redactionEnabled
        ? Log4AgentRedactor.redactUrl(message, keys: current.redactionKeys)
        : message;
    final safeAttributes = current.redactionEnabled
        ? Log4AgentRedactor.redactAttributes(
            attributes,
            keys: current.redactionKeys,
          )
        : attributes;
    final payload = jsonEncode({
      'timestamp': DateTime.now().toUtc().toIso8601String(),
      'app': current.app,
      'deviceId': current.deviceId,
      'sessionId': _sessionId,
      'platform': 'flutter',
      'level': level.name,
      'category': category,
      'message': safeMessage,
      'attributes': safeAttributes,
    });
    _transport.postJson(current.endpoint, payload).ignore();
  }

  static void _startSession() {
    final current = _config;
    if (_sessionStarted || !current.enabled || current.endpoint.isEmpty) return;
    _sessionStarted = true;
    _transport
        .postJson(
          _sessionEndpoint(current.endpoint),
          jsonEncode({
            'timestamp': DateTime.now().toUtc().toIso8601String(),
            'app': current.app,
            'deviceId': current.deviceId,
            'sessionId': _sessionId,
            'platform': 'flutter',
          }),
        )
        .ignore();
  }

  static String _sessionEndpoint(String logEndpoint) {
    if (logEndpoint.endsWith('/logs')) {
      return '${logEndpoint.substring(0, logEndpoint.length - '/logs'.length)}/sessions';
    }
    return '${logEndpoint.replaceFirst(RegExp(r'/+$'), '')}/sessions';
  }

  static String _newSessionId() {
    final now = DateTime.now().toUtc().millisecondsSinceEpoch;
    final random = Random().nextInt(0x7fffffff).toRadixString(16);
    return '$now-$random';
  }
}
