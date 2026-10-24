import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const obj = readFileSync('model/galeon_master.obj','utf8');
const mtl = readFileSync('model/galeon_master.mtl','utf8');
const vertices = [];
const faces = [];
const materials = new Set();
for (const line of obj.split('\n')) {
    if (line.startsWith('v ')) {
        const numbers = line.slice(2).split(' ').map(Number);
        assert.equal(numbers.length,3);
        assert(numbers.every(Number.isFinite),'non-finite vertex');
        vertices.push(numbers);
    } else if(line.startsWith('f ')) {
        const ids = line.slice(2).split(' ').map(Number);
        assert.equal(ids.length,3);
        assert(ids.every(n=>n>=1 && n<=vertices.length),'out-of-range vertex index');
        faces.push(ids);
    } else if(line.startsWith('usemtl ')) {
        materials.add(line.substring(7).trim());
    }
}
const definitions=new Set([...mtl.matchAll(/^newmtl (.+)$/gm)].map(m=>m[1]));
assert(vertices.length>30000,'too few mesh vertices for master model');
assert(faces.length>30000,'too few triangles');
assert(materials.size>=12,'missing materials');
for(const name of materials) assert(definitions.has(name),'undefined material '+name);
const extent = dim=>[Math.min(...vertices.map(v=>v[dim])),Math.max(...vertices.map(v=>v[dim]))];
const [minX,maxX]=extent(0),[minY,maxY]=extent(1),[minZ,maxZ]=extent(2);
assert(minX<-5 && maxX>5,'cannons must protrude from 9.5m hull');
assert(minY< -4.1 && maxY>=42.85,'draft or rig height wrong');
assert(minZ<=-25.95 && maxZ>=18,'bowsprit or stern missing');
const amidships=vertices.filter(p=>Math.abs(p[2])<1e-5 && Math.abs(p[1])<1e-5);
assert(amidships.some(p=>Math.abs(Math.abs(p[0])-4.75)<.001),'hull beam is not 9.5m');
console.log(JSON.stringify({vertices:vertices.length,triangles:faces.length,
    usedMaterials:materials.size,extents:{minX,maxX,minY,maxY,minZ,maxZ},
    hullBeamMeters:9.5,hullLengthMeters:36},null,2));
