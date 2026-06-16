import os
import re
import glob

def analyze_java_file(filepath):
    with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
        lines = f.readlines()
        
    loc = len(lines)
    imports = 0
    public_fields = 0
    max_indent = 0
    methods_with_many_params = 0
    magic_numbers = 0
    
    import_pattern = re.compile(r'^\s*import\s+')
    public_field_pattern = re.compile(r'^\s*public\s+(?!static|final|class|interface|enum|record)[\w<>,\[\]\s]+\s+\w+\s*(?:;|=[^;]+;)')
    method_param_pattern = re.compile(r'\b\w+\s*\([^)]*,[^)]*,[^)]*,[^)]*,[^)]+\)\s*\{')
    magic_number_pattern = re.compile(r'(?<![-.\w])(?:\d+)(?![.\w])') # Simple heuristic

    for line in lines:
        if import_pattern.match(line):
            imports += 1
        if public_field_pattern.match(line):
            public_fields += 1
        
        # indent
        leading_spaces = len(line) - len(line.lstrip(' '))
        if leading_spaces > max_indent:
            max_indent = leading_spaces
            
        if method_param_pattern.search(line):
            methods_with_many_params += 1
            
        # exclude obvious non-magic like 0, 1
        numbers = magic_number_pattern.findall(line)
        for n in numbers:
            if n not in ['0', '1', '2'] and not line.strip().startswith('//') and not line.strip().startswith('*'):
                magic_numbers += 1

    return {
        'path': filepath,
        'loc': loc,
        'imports': imports,
        'public_fields': public_fields,
        'max_indent': max_indent,
        'many_params': methods_with_many_params,
        'magic_numbers': magic_numbers
    }

results = []
for root, dirs, files in os.walk('.'):
    if 'build' in root or 'test' in root or 'Mock' in root or '.gradle' in root or '.git' in root or 'out' in root:
        continue
    for file in files:
        if file.endswith('.java') and not 'Test' in file:
            filepath = os.path.join(root, file)
            results.append(analyze_java_file(filepath))

print("--- TOP 10 FILES BY LOC (SIZE) ---")
for r in sorted(results, key=lambda x: x['loc'], reverse=True)[:10]:
    print(f"{r['loc']} lines - {r['path']}")

print("\n--- TOP 10 FILES BY IMPORTS (COUPLING) ---")
for r in sorted(results, key=lambda x: x['imports'], reverse=True)[:10]:
    print(f"{r['imports']} imports - {r['path']}")

print("\n--- TOP 10 FILES BY PUBLIC FIELDS (ENCAPSULATION) ---")
for r in sorted(results, key=lambda x: x['public_fields'], reverse=True)[:10]:
    print(f"{r['public_fields']} public fields - {r['path']}")

print("\n--- TOP 10 FILES BY MAX INDENTATION (COMPLEXITY) ---")
for r in sorted(results, key=lambda x: x['max_indent'], reverse=True)[:10]:
    print(f"{r['max_indent']} spaces indent - {r['path']}")

print("\n--- TOP 10 FILES BY METHODS WITH >4 PARAMS ---")
for r in sorted(results, key=lambda x: x['many_params'], reverse=True)[:10]:
    print(f"{r['many_params']} methods - {r['path']}")

print("\n--- TOP 10 FILES BY MAGIC NUMBERS ---")
for r in sorted(results, key=lambda x: x['magic_numbers'], reverse=True)[:10]:
    print(f"{r['magic_numbers']} magic numbers - {r['path']}")
