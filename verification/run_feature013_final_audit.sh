#!/usr/bin/env bash
set -u
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

commands=(
  "python3 verification/verify_local_compose_optimization_v16.py"
  "python3 verification/verify_compose_runtime_network_v13.py"
  "python3 ci-cdconfigs/verify_compose_runtime_paths_v11.py"
  "python3 verification/verify_java25_usecase_evidence.py"
  "python3 verification/verify_catalog_jpa_final.py"
  "python3 verification/verify_checkpoint4_checkout_promotion.py"
  "python3 verification/verify_checkpoint5_resilience_architecture.py"
  "python3 verification/verify_checkpoint6_outbox_recovery.py"
  "python3 verification/verify_controller_persistence_boundary_final.py"
  "python3 verification/verify_database_assets.py"
  "python3 verification/verify_deep_refactor.py"
  "python3 verification/verify_feature_013_active_usecases.py"
  "python3 verification/verify_feature_013_depth.py"
  "python3 verification/verify_feature_013_quality_gate.py"
  "python3 verification/verify_final_compile_integration_risks.py"
  "python3 verification/verify_media_seed.py"
  "python3 verification/verify_named_exceptions_final.py"
  "python3 verification/verify_observability_assets.py"
  "python3 verification/verify_operational_hardening_final.py"
  "python3 verification/verify_platform_abstractions.py"
  "python3 verification/verify_platform_core_i18n_enum.py"
  "python3 verification/verify_platform_mapstruct_contract.py"
  "python3 verification/verify_requested_hardening_v6.py"
  "python3 verification/verify_compiler_followups_v7.py"
  "python3 verification/verify_payment_compile_followups_v8.py"
  "python3 verification/verify_frontend_docker_database_labs_v9.py"
  "python3 verification/verify_remaining_unit_test_alignment_v10.py"
  "python3 ci-cdconfigs/verify_frontend_dba_compose_v11.py"
  "bash verification/verify_production_depth_smokes.sh"
  "python3 verification/verify_realistic_data_sql_labs.py"
  "python3 verification/verify_runtime_flow_depth.py"
  "python3 verification/verify_seed_integrity.py"
  "python3 verification/verify_service_depth_final.py"
  "python3 verification/verify_typed_redis_cache.py"
  "python3 verification/verify_usecase_naming_final.py"
  "bash verification/verify-naming-internal-config.sh"
  "python3 verification/verify_ci_config_coverage.py"
  "python3 verification/verify_api_search_contract_v7.py"
  "python3 verification/verify_maven_model_v5.py"
  "python3 verification/verify_java_text_block_syntax.py"
  "python3 verification/verify_source_hygiene_v8.py"
  "python3 verification/verify_dependency_hygiene_v9.py"
  "python3 verification/verify_java_source_quality_v10.py"
  "python3 verification/verify_annotation_mapper_import_hygiene_v11.py"
  "python3 verification/verify_model_import_annotation_hygiene_v12.py"
)

overall=0
for command in "${commands[@]}"; do
  echo "===== RUN ${command} ====="
  bash -lc "$command"
  code=$?
  echo "===== EXIT ${code} : ${command} ====="
  echo
  if [[ $code -ne 0 ]]; then
    overall=$code
  fi
done

echo "OVERALL=${overall}"
exit "$overall"

