import os
import re
from collections import defaultdict

extends_pattern = re.compile(r'class\s+\w+\s+extends\s+([A-Z]\w+)')
implements_pattern = re.compile(r'class\s+\w+\s+(?:extends\s+\w+\s+)?implements\s+([\w\s,]+)')
class_pattern = re.compile(r'class\s+([A-Z]\w+)')

parent_count = defaultdict(int)
class_hierarchy = {}

for root, dirs, files in os.walk('.'):
    if 'build' in root or 'test' in root or 'Mock' in root or '.gradle' in root or '.git' in root or 'out' in root:
        continue
    for file in files:
        if file.endswith('.java') and not 'Test' in file:
            filepath = os.path.join(root, file)
            with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
                content = f.read()
                
                class_match = class_pattern.search(content)
                if class_match:
                    class_name = class_match.group(1)
                    
                    extends_match = extends_pattern.search(content)
                    if extends_match:
                        parent = extends_match.group(1)
                        parent_count[parent] += 1
                        class_hierarchy[class_name] = parent

print("--- TOP 10 CLASSES BY NUMBER OF SUBCLASSES (NOC) ---")
for parent, count in sorted(parent_count.items(), key=lambda x: x[1], reverse=True)[:10]:
    print(f"{count} subclasses - {parent}")

print("\n--- FINDING DEEP INHERITANCE (DIT HEURISTIC) ---")
def get_depth(cls, current_depth=0):
    if cls not in class_hierarchy or current_depth > 10:
        return current_depth
    return get_depth(class_hierarchy[cls], current_depth + 1)

depths = {}
for cls in class_hierarchy.keys():
    depths[cls] = get_depth(cls)
    
for cls, depth in sorted(depths.items(), key=lambda x: x[1], reverse=True)[:10]:
    if depth > 1:
        path = []
        c = cls
        while c in class_hierarchy:
            path.append(c)
            c = class_hierarchy[c]
        path.append(c)
        print(f"Depth {depth}: {' -> '.join(path)}")

